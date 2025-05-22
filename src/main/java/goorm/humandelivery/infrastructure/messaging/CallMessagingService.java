package goorm.humandelivery.infrastructure.messaging;

import goorm.humandelivery.domain.model.internal.CallMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallMessagingService {
    private final SimpMessagingTemplate messagingTemplate;
    private static final String CALL_QUEUE = "/queue/call";

    public void sendCallMessageToTaxiDriver(String driverLoginId, CallMessage message) {
        messagingTemplate.convertAndSendToUser(driverLoginId, CALL_QUEUE, message);
    }

}