package goorm.humandelivery.shared.messaging.kafka;

import goorm.humandelivery.shared.messaging.QueueMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private final KafkaTemplate<String, QueueMessage> kafkaTemplate;
    private final String topicName = "taxi-call-queue";

    public void send(QueueMessage message) {
        kafkaTemplate.send(topicName, message);
    }

}
