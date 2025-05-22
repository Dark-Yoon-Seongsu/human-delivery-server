package goorm.humandelivery.shared.messaging.kafka;

import goorm.humandelivery.shared.application.port.out.MessageQueuePort;
import goorm.humandelivery.shared.messaging.QueueMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageConsumer {
    private final MessageQueuePort messageQueuePort;

    @KafkaListener(topics = "taxi-call-queue", groupId = "call-group")
    public void listen(QueueMessage message) {
        log.info("콜 메시지 큐에서 메시지 수신");
        messageQueuePort.processMessage(message);
    }
}
