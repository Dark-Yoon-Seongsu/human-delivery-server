package goorm.humandelivery.driving.application.port.out;

import goorm.humandelivery.driving.dto.response.DrivingInfoResponse;

public interface SendDrivingStartToDriverPort {

    void sendToDriver(String taxiDriverLoginId, DrivingInfoResponse response);

}