package goorm.humandelivery.call.application;
import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.call.application.port.out.SendCallCancelToDriverPort;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.driver.domain.TaxiDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyCallCancelToDriverService {

    private final SendCallCancelToDriverPort sendCallCancelToDriverPort;

    public void notifyDriverOfCancelledCall(String driverLoginId) {
        try {
            sendCallCancelToDriverPort.sendToDriver(driverLoginId, "승객이 콜을 취소했습니다.");
            log.info("[NotifyCallCancelToDriverService] 기사 {} 에게 콜 취소 알림 전송", driverLoginId);
        } catch (Exception e) {
            log.error("[NotifyCallCancelToDriverService] 기사({})에게 콜 취소 알림 전송 실패: {}", driverLoginId, e.getMessage(), e);
        }

    }
}
