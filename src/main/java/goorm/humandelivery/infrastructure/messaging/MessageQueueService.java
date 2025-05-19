package goorm.humandelivery.infrastructure.messaging;

import goorm.humandelivery.domain.model.internal.QueueMessage;

public interface MessageQueueService {


    void enqueue(QueueMessage message);

    void processMessage();

    void processMessage(QueueMessage message);

}
