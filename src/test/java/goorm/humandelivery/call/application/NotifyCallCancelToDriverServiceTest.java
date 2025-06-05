package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.out.SendCallCancelToDriverPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class NotifyCallCancelToDriverServiceTest {

    private SendCallCancelToDriverPort sendCallCancelToDriverPort;
    private NotifyCallCancelToDriverService notifyCallCancelToDriverService;

    @BeforeEach
    void setUp() {
        sendCallCancelToDriverPort = mock(SendCallCancelToDriverPort.class);
        notifyCallCancelToDriverService = new NotifyCallCancelToDriverService(sendCallCancelToDriverPort);
    }

    @Test
    @DisplayName("✅ 정상적인 loginId로 콜 취소 메시지 전송")
    void notifyDriverOfCancelledCall_success() {
        // given
        String driverLoginId = "driver123";

        // when
        notifyCallCancelToDriverService.notifyDriverOfCancelledCall(driverLoginId);

        // then
        verify(sendCallCancelToDriverPort, times(1))
                .sendToDriver(driverLoginId, "승객이 콜을 취소했습니다.");
    }

    @Test
    @DisplayName("⚠️ 예외 발생 시에도 예외가 외부로 전파되지 않음")
    void notifyDriverOfCancelledCall_exceptionHandled() {
        // given
        String driverLoginId = "driverError";
        doThrow(new RuntimeException("WebSocket failure")).when(sendCallCancelToDriverPort)
                .sendToDriver(eq(driverLoginId), anyString());

        // when & then
        assertDoesNotThrow(() ->
                notifyCallCancelToDriverService.notifyDriverOfCancelledCall(driverLoginId)
        );

        verify(sendCallCancelToDriverPort, times(1))
                .sendToDriver(driverLoginId, "승객이 콜을 취소했습니다.");
    }
}
