package goorm.humandelivery.driving.application.port.out;

import goorm.humandelivery.driving.domain.DrivingInfo;

public interface SaveDrivingInfoPort {

    DrivingInfo save(DrivingInfo drivingInfo);
}
