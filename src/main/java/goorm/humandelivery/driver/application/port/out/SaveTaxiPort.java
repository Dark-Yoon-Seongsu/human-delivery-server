package goorm.humandelivery.driver.application.port.out;

import goorm.humandelivery.driver.domain.Taxi;

public interface SaveTaxiPort {

    Taxi save(Taxi taxi);

    void deleteAllInBatch();
}
