package goorm.humandelivery.shared.location.application.port.in;

import goorm.humandelivery.shared.location.domain.Location;

public interface GetDriverLocationUseCase {

    Location getDriverLocation(String driverLoginId);

}