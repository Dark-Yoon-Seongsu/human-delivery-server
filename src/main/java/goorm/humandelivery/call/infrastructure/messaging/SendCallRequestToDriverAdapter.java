package goorm.humandelivery.call.infrastructure.messaging;

import goorm.humandelivery.call.application.port.out.SendCallRequestToDriverPort;
import goorm.humandelivery.shared.messaging.CallMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendCallRequestToDriverAdapter implements SendCallRequestToDriverPort {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String DESTINATION = "/queue/call";

    @Override
    public void sendToDriver(String driverLoginId, CallMessage callMessage) {
        messagingTemplate.convertAndSendToUser(driverLoginId, DESTINATION, callMessage);
    }
}