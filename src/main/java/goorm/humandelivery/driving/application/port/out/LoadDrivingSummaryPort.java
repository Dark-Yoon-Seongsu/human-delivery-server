package goorm.humandelivery.driving.application.port.out;

import goorm.humandelivery.driving.domain.DrivingInfo;
import goorm.humandelivery.driving.dto.response.DrivingSummaryResponse;

import java.util.Optional;

public interface LoadDrivingSummaryPort {
    Optional<DrivingSummaryResponse> findDrivingSummaryResponse(DrivingInfo drivingInfo);
}