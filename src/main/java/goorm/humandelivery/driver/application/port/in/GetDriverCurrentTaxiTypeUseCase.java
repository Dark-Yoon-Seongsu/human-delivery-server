package goorm.humandelivery.driver.application.port.in;

import goorm.humandelivery.driver.domain.TaxiType;

public interface GetDriverCurrentTaxiTypeUseCase {

    TaxiType getCurrentTaxiType(String driverLoginId);

}
