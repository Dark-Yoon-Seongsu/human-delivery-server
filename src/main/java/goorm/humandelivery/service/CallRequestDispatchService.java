package goorm.humandelivery.service;

import goorm.humandelivery.domain.model.entity.CallStatus;
import goorm.humandelivery.domain.model.internal.CallMessage;
import goorm.humandelivery.infrastructure.redis.RedisService;
import goorm.humandelivery.infrastructure.messaging.MessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallRequestDispatchService {

    private final RedisService redisService;
    private final MessagingService messagingService;

    /**
     * 택시 드라이버에게 콜 요청을 전송하고 상태를 SENT로 설정한다.
     *
     * @param callMessage           콜 메시지
     * @param availableTaxiDrivers  요청 대상 드라이버 ID 리스트
     */
    public void dispatchCallRequest(CallMessage callMessage, List<String> availableTaxiDrivers) {

        log.info("유효한 택시기사 {}명에게 콜 요청 전송 시도", availableTaxiDrivers.size());

        if (availableTaxiDrivers.isEmpty()) {
            return;
        }

        try {
            redisService.setCallWith(callMessage.getCallId(), CallStatus.SENT);
        } catch (Exception e) {
            log.error("콜 상태 저장 실패", e);
            throw new RuntimeException("Redis 저장 실패", e);
        }

        for (String taxiDriverLoginId : availableTaxiDrivers) {
            try {
                messagingService.sendCallMessageToTaxiDriver(taxiDriverLoginId, callMessage);
            } catch (Exception e) {
                log.warn("택시기사({})에게 메시지 전송 실패: {}", taxiDriverLoginId, e.getMessage());
            }
        }

        log.info("유효한 택시기사 {}명에게 콜 요청 전송 완료", availableTaxiDrivers.size());
    }
}
