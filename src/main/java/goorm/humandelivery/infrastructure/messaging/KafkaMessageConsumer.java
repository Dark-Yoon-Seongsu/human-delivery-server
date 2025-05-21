package goorm.humandelivery.infrastructure.messaging;

import goorm.humandelivery.shared.messaging.QueueMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageConsumer {
    private final KafkaMessageQueueService kafkaMessageQueueService;

    @KafkaListener(topics = "taxi-call-queue", groupId = "call-group")
    public void listen(QueueMessage message) {
        log.info("콜 메시지 큐에서 메시지 수신");
        kafkaMessageQueueService.processMessage(message);
    }
}
