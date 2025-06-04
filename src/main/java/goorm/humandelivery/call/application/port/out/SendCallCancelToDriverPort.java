package goorm.humandelivery.call.application.port.out;

public interface SendCallCancelToDriverPort {
    void sendToDriver(String taxiDriverLoginId, String message);
}
