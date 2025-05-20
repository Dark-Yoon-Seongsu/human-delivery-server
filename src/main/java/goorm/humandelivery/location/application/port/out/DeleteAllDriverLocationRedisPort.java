package goorm.humandelivery.location.application.port.out;

import goorm.humandelivery.driver.domain.TaxiType;

public interface DeleteAllDriverLocationRedisPort {

    void deleteAllLocationData(String driverLoginId, TaxiType taxiType);

}