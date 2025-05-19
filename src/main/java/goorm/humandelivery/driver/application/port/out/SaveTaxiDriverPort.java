package goorm.humandelivery.driver.application.port.out;

import goorm.humandelivery.driver.domain.TaxiDriver;

public interface SaveTaxiDriverPort {

    TaxiDriver save(TaxiDriver taxiDriver);

    boolean existsByLoginId(String loginId);

    void deleteAllInBatch();

}
