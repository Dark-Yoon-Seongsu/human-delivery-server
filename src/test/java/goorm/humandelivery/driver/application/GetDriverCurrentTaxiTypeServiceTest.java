package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.out.LoadTaxiDriverTypePort;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.response.TaxiTypeResponse;
import goorm.humandelivery.global.exception.DriverEntityNotFoundException;
import goorm.humandelivery.shared.application.port.out.GetValuePort;
import goorm.humandelivery.shared.application.port.out.SetValueWithTtlPort;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//./gradlew test --tests "goorm.humandelivery.driver.application.GetDriverCurrentTaxiTypeServiceTest"
@ExtendWith(MockitoExtension.class)
class GetDriverCurrentTaxiTypeServiceTest {

    private GetValuePort getValuePort;
    private SetValueWithTtlPort setValueWithTtlPort;
    private LoadTaxiDriverTypePort loadTaxiDriverTypePort;
    private GetDriverCurrentTaxiTypeService service;

    @BeforeEach
    void setUp() {
        getValuePort = mock(GetValuePort.class);
        setValueWithTtlPort = mock(SetValueWithTtlPort.class);
        loadTaxiDriverTypePort = mock(LoadTaxiDriverTypePort.class);
        service = new GetDriverCurrentTaxiTypeService(getValuePort, setValueWithTtlPort, loadTaxiDriverTypePort);
    }

    @Test
    @DisplayName("Redis에 캐시된 택시 타입이 있을 경우 해당 값을 반환한다")
    void getCurrentTaxiType_fromRedis() {
        String driverId = "driver1";
        String key = RedisKeyParser.taxiDriversTaxiType(driverId);
        when(getValuePort.getValue(key)).thenReturn("NORMAL");

        TaxiType taxiType = service.getCurrentTaxiType(driverId);

        assertEquals(TaxiType.NORMAL, taxiType);
        verify(loadTaxiDriverTypePort, never()).findTaxiDriversTaxiTypeByLoginId(any());
        verify(setValueWithTtlPort, never()).setValueWithTTL(any(), any(), any());
    }

    @Test
    @DisplayName("Redis에 없고 DB에 드라이버 정보가 있을 경우 DB에서 조회 후 Redis에 저장하고 반환한다")
    void getCurrentTaxiType_fromDbAndCacheIt() {
        String driverId = "driver1";
        String key = RedisKeyParser.taxiDriversTaxiType(driverId);
        when(getValuePort.getValue(key)).thenReturn(null);

        TaxiTypeResponse response = new TaxiTypeResponse(TaxiType.NORMAL);
        when(loadTaxiDriverTypePort.findTaxiDriversTaxiTypeByLoginId(driverId)).thenReturn(Optional.of(response));

        TaxiType taxiType = service.getCurrentTaxiType(driverId);

        assertEquals(TaxiType.NORMAL, taxiType);

        // Redis에 저장할 때 key, 값, TTL 확인
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);

        verify(setValueWithTtlPort).setValueWithTTL(keyCaptor.capture(), valueCaptor.capture(), durationCaptor.capture());

        assertEquals(key, keyCaptor.getValue());
        assertEquals("NORMAL", valueCaptor.getValue());
        assertEquals(Duration.ofDays(1), durationCaptor.getValue());
    }

    @Test
    @DisplayName("Redis에도 없고 DB에도 드라이버 정보가 없으면 예외가 발생한다")
    void getCurrentTaxiType_notFound() {
        String driverId = "driver1";
        String key = RedisKeyParser.taxiDriversTaxiType(driverId);
        when(getValuePort.getValue(key)).thenReturn(null);
        when(loadTaxiDriverTypePort.findTaxiDriversTaxiTypeByLoginId(driverId)).thenReturn(Optional.empty());

        assertThrows(DriverEntityNotFoundException.class, () -> service.getCurrentTaxiType(driverId));
    }
}
