package goorm.humandelivery.driving.infrastructure.messaging;

import goorm.humandelivery.driving.application.port.out.SendDrivingCompletedToCustomerPort;
import goorm.humandelivery.driving.dto.response.DrivingSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendDrivingCompletedToCustomerAdapter implements SendDrivingCompletedToCustomerPort {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String DRIVING_FINISH_TO_USER = "/queue/driving-finish";

    @Override
    public void sendToCustomer(String customerLoginId, DrivingSummaryResponse response) {
        messagingTemplate.convertAndSendToUser(customerLoginId, DRIVING_FINISH_TO_USER, response);
    }
}