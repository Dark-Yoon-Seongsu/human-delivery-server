package goorm.humandelivery.driver.application.port.out;

import goorm.humandelivery.driver.domain.TaxiDriver;

import java.util.Optional;

public interface LoadTaxiDriverPort {

    Optional<TaxiDriver> findByLoginId(String loginId);
}
