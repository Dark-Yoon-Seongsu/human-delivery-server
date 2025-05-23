package goorm.humandelivery.shared.location.application.port.out;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;

public interface RemoveFromLocationPort {

    void removeFromLocation(String driverLoginId, TaxiType taxiType, TaxiDriverStatus status);

}