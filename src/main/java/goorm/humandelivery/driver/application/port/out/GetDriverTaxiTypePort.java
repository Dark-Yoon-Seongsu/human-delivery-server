package goorm.humandelivery.driver.application.port.out;

import goorm.humandelivery.driver.domain.TaxiType;

public interface GetDriverTaxiTypePort {

    TaxiType getDriverTaxiType(String taxiDriverLoginId);

}
