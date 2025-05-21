package goorm.humandelivery.driving.application.port.in;

import goorm.humandelivery.driving.dto.response.DrivingSummaryResponse;
import goorm.humandelivery.shared.location.domain.Location;

public interface FinishDrivingUseCase {

    DrivingSummaryResponse finishDriving(Long callId, Location destination);

}