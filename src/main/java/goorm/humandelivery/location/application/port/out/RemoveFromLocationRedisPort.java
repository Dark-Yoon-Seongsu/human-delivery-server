package goorm.humandelivery.location.application.port.out;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;

public interface RemoveFromLocationRedisPort {

    void removeFromLocation(String driverLoginId, TaxiType taxiType, TaxiDriverStatus status);

}