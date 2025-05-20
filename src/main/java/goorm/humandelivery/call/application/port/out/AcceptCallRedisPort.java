package goorm.humandelivery.call.application.port.out;

public interface AcceptCallRedisPort {

    void atomicAcceptCall(Long callId, String driverLoginId);

}
