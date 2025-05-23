package goorm.humandelivery.call.application.port.out;

public interface RemoveRejectedDriversForCallPort {

    void removeRejectedDrivers(Long callId);

}