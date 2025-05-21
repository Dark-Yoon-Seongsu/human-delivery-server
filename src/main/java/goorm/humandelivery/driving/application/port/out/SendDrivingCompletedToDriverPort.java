package goorm.humandelivery.driving.application.port.out;

import goorm.humandelivery.driving.dto.response.DrivingSummaryResponse;

public interface SendDrivingCompletedToDriverPort {

    void sendToDriver(String taxiDriverLoginId, DrivingSummaryResponse response);

}