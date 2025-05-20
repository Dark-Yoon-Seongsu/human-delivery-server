package goorm.humandelivery.call.application.port.out;

public interface AddRejectedDriverToCallRedisPort {

    void addRejectedDriverToCall(Long callId, String driverLoginId);

}