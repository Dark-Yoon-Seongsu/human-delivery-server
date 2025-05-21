package goorm.humandelivery.driver.application.port.out;

import goorm.humandelivery.driver.dto.response.TaxiTypeResponse;

import java.util.Optional;

public interface LoadTaxiDriverTypePort {

    Optional<TaxiTypeResponse> findTaxiDriversTaxiTypeByLoginId(String loginId);

}