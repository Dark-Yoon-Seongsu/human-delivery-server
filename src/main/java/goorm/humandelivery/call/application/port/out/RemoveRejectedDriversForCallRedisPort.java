package goorm.humandelivery.call.application.port.out;

public interface RemoveRejectedDriversForCallRedisPort {

    void removeRejectedDrivers(Long callId);

}