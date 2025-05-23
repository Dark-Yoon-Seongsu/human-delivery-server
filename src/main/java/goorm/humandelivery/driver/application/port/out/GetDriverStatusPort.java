package goorm.humandelivery.driver.application.port.out;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;

public interface GetDriverStatusPort {

    TaxiDriverStatus getDriverStatus(String driverId);

}
