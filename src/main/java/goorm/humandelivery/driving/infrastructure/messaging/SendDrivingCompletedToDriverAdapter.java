package goorm.humandelivery.driving.infrastructure.messaging;

import goorm.humandelivery.driving.application.port.out.SendDrivingCompletedToDriverPort;
import goorm.humandelivery.driving.dto.response.DrivingSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendDrivingCompletedToDriverAdapter implements SendDrivingCompletedToDriverPort {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String DISPATCH_DRIVING_RESULT_MESSAGE = "/queue/driving-result";

    @Override
    public void sendToDriver(String taxiDriverLoginId, DrivingSummaryResponse response) {
        log.info("[sendDrivingCompletedMessageToTaxiDriver.MessagingService] 택시 기사에게 손님 하차 응답 메세지 전송.  콜 ID : {}, 택시기사 ID : {}", response.getCallId(), taxiDriverLoginId);
        log.info("DrivingSummaryResponse : {}", response.toString());

        messagingTemplate.convertAndSendToUser(taxiDriverLoginId, DISPATCH_DRIVING_RESULT_MESSAGE, response);
    }
}