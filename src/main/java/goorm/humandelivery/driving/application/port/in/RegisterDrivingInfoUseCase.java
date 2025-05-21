package goorm.humandelivery.driving.application.port.in;

import goorm.humandelivery.driving.domain.DrivingInfo;
import goorm.humandelivery.driving.dto.request.CreateDrivingInfoRequest;

public interface RegisterDrivingInfoUseCase {

    DrivingInfo create(CreateDrivingInfoRequest request);

}