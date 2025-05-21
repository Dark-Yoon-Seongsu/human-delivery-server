package goorm.humandelivery.call.infrastructure.messaging;

import goorm.humandelivery.call.application.port.out.SendDispatchFailToCustomerPort;
import goorm.humandelivery.shared.dto.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendDispatchFailToCustomerAdapter implements SendDispatchFailToCustomerPort {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String DISPATCH_FAIL_MESSAGE_TO_USER = "/queue/dispatch-error";

    @Override
    public void sendToCustomer(String customerLoginId, ErrorResponse response) {
        messagingTemplate.convertAndSendToUser(customerLoginId, DISPATCH_FAIL_MESSAGE_TO_USER, response);
    }
}