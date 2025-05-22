package goorm.humandelivery.infrastructure.messaging;

import goorm.humandelivery.domain.model.internal.CallMessage;
import goorm.humandelivery.domain.model.internal.QueueMessage;
import goorm.humandelivery.infrastructure.messaging.handler.CallMessageHandler;
import goorm.humandelivery.infrastructure.messaging.kafka.KafkaMessageProducer;
import goorm.humandelivery.infrastructure.messaging.kafka.KafkaMessageQueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

//./gradlew test --tests "goorm.humandelivery.infrastructure.messaging.KafkaMessageQueueServiceTest"
class KafkaMessageQueueServiceTest {

    private KafkaMessageProducer kafkaMessageProducer;
    private CallMessageHandler callMessageHandler;
    private KafkaMessageQueueService kafkaMessageQueueService;

    @BeforeEach
    void setUp() {
        kafkaMessageProducer = mock(KafkaMessageProducer.class);
        callMessageHandler = mock(CallMessageHandler.class);

        kafkaMessageQueueService = new KafkaMessageQueueService(
                kafkaMessageProducer,
                callMessageHandler
        );
    }

    @Test
    void enqueue_sendsMessageThroughProducer() {
        // given
        QueueMessage message = mock(QueueMessage.class);

        // when
        kafkaMessageQueueService.enqueue(message);

        // then
        verify(kafkaMessageProducer).send(message);
    }

    @Test
    void processMessage_delegatesToCallMessageHandler_ifCallMessage() {
        // given
        CallMessage callMessage = mock(CallMessage.class);

        // when
        kafkaMessageQueueService.processMessage(callMessage);

        // then
        verify(callMessageHandler).handle(callMessage);
    }

    @Test
    void processMessage_doesNothing_ifNotCallMessage() {
        // given
        QueueMessage nonCallMessage = mock(QueueMessage.class);

        // when
        kafkaMessageQueueService.processMessage(nonCallMessage);

        // then
        verifyNoInteractions(callMessageHandler);
    }
}
