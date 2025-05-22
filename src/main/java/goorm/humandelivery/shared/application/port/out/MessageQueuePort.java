package goorm.humandelivery.shared.application.port.out;

import goorm.humandelivery.shared.messaging.QueueMessage;

public interface MessageQueuePort {

    void enqueue(QueueMessage message);

    void processMessage(QueueMessage message);

}
