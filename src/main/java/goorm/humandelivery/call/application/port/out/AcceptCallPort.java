package goorm.humandelivery.call.application.port.out;

public interface AcceptCallPort {

    void atomicAcceptCall(Long callId, String driverLoginId);

}
