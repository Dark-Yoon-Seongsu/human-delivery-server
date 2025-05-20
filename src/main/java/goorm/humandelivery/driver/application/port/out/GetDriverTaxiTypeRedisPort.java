package goorm.humandelivery.driver.application.port.out;

import goorm.humandelivery.driver.domain.TaxiType;

public interface GetDriverTaxiTypeRedisPort {

    TaxiType getDriverTaxiType(String taxiDriverLoginId);

}
