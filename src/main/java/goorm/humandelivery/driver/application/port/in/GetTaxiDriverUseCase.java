package goorm.humandelivery.driver.application.port.in;

import goorm.humandelivery.driver.domain.TaxiDriver;

public interface GetTaxiDriverUseCase {

    TaxiDriver findById(Long id);

    Long findIdByLoginId(String taxiDriverLoginId);

}
