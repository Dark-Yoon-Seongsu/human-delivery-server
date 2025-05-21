package goorm.humandelivery.driving.application.port.out;

import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.driving.domain.DrivingInfo;

import java.util.Optional;

public interface LoadDrivingInfoPort {

    Optional<DrivingInfo> findDrivingInfoByMatching(Matching matching);

}