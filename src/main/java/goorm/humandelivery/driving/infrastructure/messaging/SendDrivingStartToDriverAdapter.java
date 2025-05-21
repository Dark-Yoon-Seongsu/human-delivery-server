package goorm.humandelivery.driving.infrastructure.messaging;

import goorm.humandelivery.driving.application.port.out.SendDrivingStartToDriverPort;
import goorm.humandelivery.driving.dto.response.DrivingInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendDrivingStartToDriverAdapter implements SendDrivingStartToDriverPort {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String DISPATCH_DRIVING_STATUS_MESSAGE = "/queue/ride-status";

    @Override
    public void sendToDriver(String taxiDriverLoginId, DrivingInfoResponse response) {
        messagingTemplate.convertAndSendToUser(taxiDriverLoginId, DISPATCH_DRIVING_STATUS_MESSAGE, response);
    }
}