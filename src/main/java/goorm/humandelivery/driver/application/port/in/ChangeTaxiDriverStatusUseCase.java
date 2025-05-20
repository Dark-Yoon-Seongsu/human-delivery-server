package goorm.humandelivery.driver.application.port.in;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;

public interface ChangeTaxiDriverStatusUseCase {

    TaxiDriverStatus changeStatus(String loginId, TaxiDriverStatus status);

}
