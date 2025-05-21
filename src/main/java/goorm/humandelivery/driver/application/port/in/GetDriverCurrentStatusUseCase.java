package goorm.humandelivery.driver.application.port.in;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;

public interface GetDriverCurrentStatusUseCase {

    TaxiDriverStatus getCurrentStatus(String driverLoginId);

}