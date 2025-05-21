package goorm.humandelivery.driver.application.port.in;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.response.UpdateTaxiDriverStatusResponse;

public interface HandleDriverStatusUseCase {

    UpdateTaxiDriverStatusResponse handleTaxiDriverStatusInRedis(String driverId, TaxiDriverStatus status, TaxiType type);

}