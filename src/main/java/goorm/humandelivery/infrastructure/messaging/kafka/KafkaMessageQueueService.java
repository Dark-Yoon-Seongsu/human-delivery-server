package goorm.humandelivery.infrastructure.messaging.kafka;

import goorm.humandelivery.infrastructure.messaging.handler.CallMessageHandler;
import org.springframework.stereotype.Service;
import goorm.humandelivery.domain.model.internal.CallMessage;
import goorm.humandelivery.domain.model.internal.QueueMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageQueueService implements MessageQueueService {

	private final KafkaMessageProducer kafkaMessageProducer;
	private final CallMessageHandler callMessageHandler;

	@Override
	public void enqueue(QueueMessage message) {
		kafkaMessageProducer.send(message);
	}

	@Override
	public void processMessage() {
		// Not used for Kafka
	}

	@Override
	public void processMessage(QueueMessage message) {
		if (message instanceof CallMessage callMessage) {
			callMessageHandler.handle(callMessage);
		}
	}
}
