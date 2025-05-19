package goorm.humandelivery.driver.infrastructure.persistence;

import goorm.humandelivery.driver.application.port.out.SaveTaxiPort;
import goorm.humandelivery.driver.domain.Taxi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTaxiRepository extends
        JpaRepository<Taxi, Long>,
        SaveTaxiPort {
}
