package goorm.humandelivery.call.application.port.out;

public interface CheckDriverRejectedForCallPort {

    boolean isDriverRejected(Long callId, String driverLoginId);

}