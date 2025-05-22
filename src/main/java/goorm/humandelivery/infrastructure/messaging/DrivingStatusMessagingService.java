package goorm.humandelivery.infrastructure.messaging;

import goorm.humandelivery.domain.model.response.DrivingInfoResponse;
import goorm.humandelivery.domain.model.response.DrivingSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrivingStatusMessagingService {
    private final SimpMessagingTemplate messagingTemplate;

    private static final String DRIVING_START_TO_USER = "/queue/driving-start";
    private static final String DISPATCH_DRIVING_STATUS_MESSAGE = "/queue/ride-status";
    private static final String DRIVING_FINISH_TO_USER = "/queue/driving-finish";
    private static final String DISPATCH_DRIVING_RESULT_MESSAGE = "/queue/driving-result";


    public void sendDrivingStartMessageToUser(String customerLoginId, boolean isStarted, boolean isFinished) {
        messagingTemplate.convertAndSendToUser(
                customerLoginId,
                DRIVING_START_TO_USER,
                new DrivingInfoResponse(isStarted, isFinished)
        );
    }


    public void sendDrivingStartMessageToTaxiDriver(String driverLoginId, boolean isStarted, boolean isFinished) {
        messagingTemplate.convertAndSendToUser(
                driverLoginId,
                DISPATCH_DRIVING_STATUS_MESSAGE,
                new DrivingInfoResponse(isStarted, isFinished)
        );
    }


    public void sendDrivingCompletedMessageToUser(String customerLoginId, DrivingSummaryResponse response) {
        messagingTemplate.convertAndSendToUser(customerLoginId, DRIVING_FINISH_TO_USER, response);
    }


    public void sendDrivingCompletedMessageToTaxiDriver(String driverLoginId, DrivingSummaryResponse response) {
        log.info("[sendCompletedToDriver] 콜 ID: {}, 택시기사 ID: {}", response.getCallId(), driverLoginId);
        messagingTemplate.convertAndSendToUser(driverLoginId, DISPATCH_DRIVING_RESULT_MESSAGE, response);
    }


}