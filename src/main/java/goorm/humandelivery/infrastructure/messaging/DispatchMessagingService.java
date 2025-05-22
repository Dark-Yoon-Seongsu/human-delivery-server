package goorm.humandelivery.infrastructure.messaging;

import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.response.ErrorResponse;
import goorm.humandelivery.domain.model.response.MatchingSuccessResponse;
import goorm.humandelivery.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchMessagingService {
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;

    private static final String DISPATCH_SUCCESS_TO_USER = "/queue/dispatch-status";
    private static final String DISPATCH_FAIL_MESSAGE_TO_USER = "/queue/dispatch-error";
    private static final String DISPATCH_FAIL_MESSAGE_TO_TAXI_DRIVER = "/queue/dispatch-canceled";



    public void sendDispatchFailMessageToUser(String customerLoginId) {
        messagingTemplate.convertAndSendToUser(
                customerLoginId,
                DISPATCH_FAIL_MESSAGE_TO_USER,
                new ErrorResponse("배차실패", "택시와 연결이 끊어졌습니다. 다시 배차를 시도합니다.")
        );
    }


    public void sendDispatchFailMessageToTaxiDriver(String driverLoginId) {
        messagingTemplate.convertAndSendToUser(
                driverLoginId,
                DISPATCH_FAIL_MESSAGE_TO_TAXI_DRIVER,
                new ErrorResponse("배차취소", "위치 미전송으로 인해 배차가 취소되었습니다.")
        );
    }


    public void notifyDispatchSuccessToCustomer(String customerLoginId, String driverLoginId) {
        TaxiDriverStatus driverStatus = redisService.getDriverStatus(driverLoginId);
        messagingTemplate.convertAndSendToUser(
                customerLoginId,
                DISPATCH_SUCCESS_TO_USER,
                new MatchingSuccessResponse(driverStatus, driverLoginId)
        );
    }


    public void notifyDispatchFailedToCustomer(String customerLoginId) {
        log.info("[notifyDispatchFailedToCustomer] 배차 실패 고객 ID: {}", customerLoginId);
        messagingTemplate.convertAndSendToUser(
                customerLoginId,
                DISPATCH_FAIL_MESSAGE_TO_USER,
                new ErrorResponse("배차취소", "범위 내에 택시가 없습니다. 잠시 후에 시도해주세요.")
        );
    }

}
