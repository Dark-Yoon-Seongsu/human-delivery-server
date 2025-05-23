package goorm.humandelivery.driver.application.port.out;

import goorm.humandelivery.driver.domain.TaxiType;

public interface SetDriverTaxiTypePort {

    void setDriverTaxiType(String driverLoginId, TaxiType taxiType);

}
