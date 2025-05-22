package goorm.humandelivery.infrastructure.messaging.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import goorm.humandelivery.domain.model.internal.QueueMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducer {

	private final KafkaTemplate<String, QueueMessage> kafkaTemplate;
	private final String topicName = "taxi-call-queue";

	public void send(QueueMessage message) {
		kafkaTemplate.send(topicName, message);
	}

}
