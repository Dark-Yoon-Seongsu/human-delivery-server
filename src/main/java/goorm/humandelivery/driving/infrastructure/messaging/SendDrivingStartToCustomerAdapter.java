package goorm.humandelivery.driving.infrastructure.messaging;

import goorm.humandelivery.driving.application.port.out.SendDrivingStartToCustomerPort;
import goorm.humandelivery.driving.dto.response.DrivingInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendDrivingStartToCustomerAdapter implements SendDrivingStartToCustomerPort {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String DRIVING_START_TO_USER = "/queue/driving-start";

    @Override
    public void sendToCustomer(String customerLoginId, DrivingInfoResponse response) {
        messagingTemplate.convertAndSendToUser(customerLoginId, DRIVING_START_TO_USER, response);
    }
}