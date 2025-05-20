package goorm.humandelivery.driver.application.port.out;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;

public interface SetDriverStatusRedisPort {

    void setDriverStatus(String driverLoginId, TaxiDriverStatus status);

}
