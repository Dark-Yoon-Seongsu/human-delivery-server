package goorm.humandelivery.driver.application.port.out;

import goorm.humandelivery.driver.domain.TaxiDriver;

import java.util.Optional;

public interface LoadTaxiDriverPort {

    Optional<TaxiDriver> findById(Long id);

    Optional<TaxiDriver> findTaxiDriverByLoginId(String taxiDriverLoginId);

    Optional<Long> findIdByLoginId(String taxiDriverLoginId);
}
