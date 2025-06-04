package goorm.humandelivery.call.infrastructure.messaging;

import goorm.humandelivery.call.application.port.out.SendCallCancelToDriverPort;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendCallCancelToDriverAdapter implements SendCallCancelToDriverPort {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String CANCEL_MESSAGE_DESTINATION = "/queue/call/cancelled";

    @Override
    public void sendToDriver(String taxiDriverLoginId, String message) {
        messagingTemplate.convertAndSendToUser(taxiDriverLoginId, CANCEL_MESSAGE_DESTINATION, message);
    }
}
