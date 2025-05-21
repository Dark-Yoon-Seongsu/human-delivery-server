package goorm.humandelivery.driver.application.port.in;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.shared.location.domain.Location;

public interface SendDriverLocationToCustomerUseCase {

    void sendLocation(String taxiDriverLoginId, TaxiDriverStatus status, TaxiType taxiType, String customerLoginId, Location location);

}