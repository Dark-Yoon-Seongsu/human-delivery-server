package goorm.humandelivery.infrastructure.messaging;

import goorm.humandelivery.shared.messaging.QueueMessage;

public interface MessageQueueService {


    void enqueue(QueueMessage message);

    void processMessage();

    void processMessage(QueueMessage message);

}
