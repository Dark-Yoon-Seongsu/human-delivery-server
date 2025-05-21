package goorm.humandelivery.call.infrastructure.messaging;

import goorm.humandelivery.call.application.port.out.SendDispatchSuccessToCustomerPort;
import goorm.humandelivery.call.dto.response.MatchingSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendDispatchSuccessToCustomerAdapter implements SendDispatchSuccessToCustomerPort {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String DISPATCH_SUCCESS_TO_USER = "/queue/dispatch-status";

    @Override
    public void sendToCustomer(String customerLoginId, MatchingSuccessResponse response) {
        messagingTemplate.convertAndSendToUser(customerLoginId, DISPATCH_SUCCESS_TO_USER, response);
    }
}