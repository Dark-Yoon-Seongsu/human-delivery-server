package goorm.humandelivery.driver.infrastructure.messaging;

import goorm.humandelivery.driver.dto.response.DriverLocationResponse;
import goorm.humandelivery.driver.application.port.out.SendDriverLocationToCustomerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendDriverLocationToCustomerAdapter implements SendDriverLocationToCustomerPort {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String LOCATION_TO_USER = "/queue/update-taxidriver-location";

    @Override
    public void sendToCustomer(String customerLoginId, DriverLocationResponse response) {
        messagingTemplate.convertAndSendToUser(customerLoginId, LOCATION_TO_USER, response);
    }
}