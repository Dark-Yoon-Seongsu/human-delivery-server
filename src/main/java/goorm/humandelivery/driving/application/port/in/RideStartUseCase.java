package goorm.humandelivery.driving.application.port.in;

public interface RideStartUseCase {

    void rideStart(Long callId, String taxiDriverLoginId);

}