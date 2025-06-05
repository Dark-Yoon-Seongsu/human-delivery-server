package goorm.humandelivery.driver.application.port.out;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;

public interface UpdateTaxiDriverPort {
    void updateStatus(Long taxiDriverId, TaxiDriverStatus status);
}
