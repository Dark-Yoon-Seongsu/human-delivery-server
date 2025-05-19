package goorm.humandelivery.driver.infrastructure.persistence;

import goorm.humandelivery.driver.application.port.out.LoadTaxiDriverPort;
import goorm.humandelivery.driver.application.port.out.SaveTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTaxiDriverRepository extends
        JpaRepository<TaxiDriver, Long>,
        SaveTaxiDriverPort,
        LoadTaxiDriverPort {
}
