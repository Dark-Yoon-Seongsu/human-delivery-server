package goorm.humandelivery.driving.application.port.in;

public interface RideFinishUseCase {

    void finish(Long callId, String taxiDriverLoginId);

}