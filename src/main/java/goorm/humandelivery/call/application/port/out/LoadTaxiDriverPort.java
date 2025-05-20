package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.driver.domain.TaxiDriver;

import java.util.Optional;

public interface LoadTaxiDriverPort {

    Optional<TaxiDriver> findById(Long taxiDriverId);

    Optional<Long> findIdByLoginId(String loginId);

}
