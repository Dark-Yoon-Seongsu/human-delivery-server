package goorm.humandelivery.service;

import goorm.humandelivery.domain.model.entity.CallStatus;
import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.internal.CallMessage;
import goorm.humandelivery.infrastructure.messaging.MessagingService;
import goorm.humandelivery.infrastructure.redis.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//./gradlew test --tests "goorm.humandelivery.service.CallRequestDispatchServiceTest"
class CallRequestDispatchServiceTest {

    @Mock
    private RedisService redisService;

    @Mock
    private MessagingService messagingService;

    @InjectMocks
    private CallRequestDispatchService callRequestDispatchService;

    private CallMessage callMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        callMessage = CallMessage.builder()
                .callId(123L)
                .customerLoginId("testuser")
                .expectedOrigin(new Location(37.5665, 126.9780))
                .taxiType(TaxiType.NORMAL)
                .build();
    }

    @Test
    @DisplayName("TC01 - 유효한 드라이버 리스트가 있을 경우 모두에게 메시지 전송 및 상태 저장")
    void TC01_dispatchCallRequest_successful() {
        // Given
        List<String> drivers = Arrays.asList("driver1", "driver2");

        // When
        callRequestDispatchService.dispatchCallRequest(callMessage, drivers);

        // Then
        verify(redisService, times(1)).setCallWith(callMessage.getCallId(), CallStatus.SENT);
        verify(messagingService, times(1)).sendCallMessageToTaxiDriver("driver1", callMessage);
        verify(messagingService, times(1)).sendCallMessageToTaxiDriver("driver2", callMessage);
    }

    @Test
    @DisplayName("TC02 - 드라이버가 없을 경우 상태 저장 없이 메시지 전송도 없음")
    void TC02_dispatchCallRequest_withEmptyDriverList() {
        // Given
        List<String> emptyDrivers = Collections.emptyList();

        // When
        callRequestDispatchService.dispatchCallRequest(callMessage, emptyDrivers);

        // Then
        verify(redisService, never()).setCallWith(anyLong(), any());
        verify(messagingService, never()).sendCallMessageToTaxiDriver(any(), any());
    }

    @Test
    @DisplayName("TC03 - Redis 저장 실패 시 예외 발생 및 메시지 전송 수행되지 않음")
    void TC03_dispatchCallRequest_redisFails() {
        // Given
        List<String> drivers = Arrays.asList("driver1", "driver2");
        doThrow(new RuntimeException("Redis 실패")).when(redisService).setCallWith(anyLong(), any());

        // When
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            callRequestDispatchService.dispatchCallRequest(callMessage, drivers);
        });

        // Then
        assertEquals("Redis 저장 실패", ex.getMessage());
        verify(messagingService, never()).sendCallMessageToTaxiDriver(any(), any());
    }

    @Test
    @DisplayName("TC04 - 드라이버 전송 중 일부 실패해도 나머지 드라이버에게는 전송 시도")
    void TC04_dispatchCallRequest_partialMessagingFails() {
        // Given
        List<String> drivers = Arrays.asList("driver1", "driver2", "driver3");

        doThrow(new RuntimeException("driver2 실패")).when(messagingService).sendCallMessageToTaxiDriver(eq("driver2"), any());

        // When
        assertDoesNotThrow(() -> {
            callRequestDispatchService.dispatchCallRequest(callMessage, drivers);
        });

        // Then
        verify(redisService, times(1)).setCallWith(callMessage.getCallId(), CallStatus.SENT);
        verify(messagingService, times(1)).sendCallMessageToTaxiDriver("driver1", callMessage);
        verify(messagingService, times(1)).sendCallMessageToTaxiDriver("driver2", callMessage);
        verify(messagingService, times(1)).sendCallMessageToTaxiDriver("driver3", callMessage);
    }
}
