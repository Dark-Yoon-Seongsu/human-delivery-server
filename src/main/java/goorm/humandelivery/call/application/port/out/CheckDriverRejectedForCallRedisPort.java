package goorm.humandelivery.call.application.port.out;

public interface CheckDriverRejectedForCallRedisPort {

    boolean isDriverRejected(Long callId, String driverLoginId);

}