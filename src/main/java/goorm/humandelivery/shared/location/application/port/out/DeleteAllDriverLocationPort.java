package goorm.humandelivery.shared.location.application.port.out;

import goorm.humandelivery.driver.domain.TaxiType;

public interface DeleteAllDriverLocationPort {

    void deleteAllLocationData(String driverLoginId, TaxiType taxiType);

}