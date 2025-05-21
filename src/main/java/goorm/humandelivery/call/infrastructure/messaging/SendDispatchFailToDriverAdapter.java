package goorm.humandelivery.call.infrastructure.messaging;

import goorm.humandelivery.call.application.port.out.SendDispatchFailToDriverPort;
import goorm.humandelivery.shared.dto.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendDispatchFailToDriverAdapter implements SendDispatchFailToDriverPort {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String DISPATCH_FAIL_MESSAGE_TO_TAXI_DRIVER = "/queue/dispatch-canceled";

    @Override
    public void sendToDriver(String driverLoginId, ErrorResponse response) {
        messagingTemplate.convertAndSendToUser(driverLoginId, DISPATCH_FAIL_MESSAGE_TO_TAXI_DRIVER, response);
    }
}