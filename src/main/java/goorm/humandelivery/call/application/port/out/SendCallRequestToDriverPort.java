package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.shared.messaging.CallMessage;

public interface SendCallRequestToDriverPort {

    void sendToDriver(String driverLoginId, CallMessage callMessage);

}