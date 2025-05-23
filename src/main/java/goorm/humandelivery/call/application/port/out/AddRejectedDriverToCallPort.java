package goorm.humandelivery.call.application.port.out;

public interface AddRejectedDriverToCallPort {

    void addRejectedDriverToCall(Long callId, String driverLoginId);

}