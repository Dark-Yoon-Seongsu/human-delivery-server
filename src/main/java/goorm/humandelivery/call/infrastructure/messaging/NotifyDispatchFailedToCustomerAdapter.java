package goorm.humandelivery.call.infrastructure.messaging;

import goorm.humandelivery.call.application.port.out.NotifyDispatchFailedToCustomerPort;
import goorm.humandelivery.shared.dto.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotifyDispatchFailedToCustomerAdapter implements NotifyDispatchFailedToCustomerPort {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String DISPATCH_FAIL_MESSAGE_TO_USER = "/queue/dispatch-error";

    @Override
    public void sendToCustomer(String customerLoginId, ErrorResponse response) {
        log.info("[notifyDispatchFailedToCustomer.MessagingService 호출] 범위 내에 택시가 없습니다. 배차 취소 메세지 전송. 고객 아이디 : {}, 목적지 : {}", customerLoginId, DISPATCH_FAIL_MESSAGE_TO_USER);
        messagingTemplate.convertAndSendToUser(customerLoginId, DISPATCH_FAIL_MESSAGE_TO_USER, response);
    }
}