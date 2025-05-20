package goorm.humandelivery.location.application.port.in;

import goorm.humandelivery.domain.model.entity.Location;

public interface GetDriverLocationUseCase {

    Location getDriverLocation(String driverLoginId);

}