package goorm.humandelivery.driver.infrastructure.persistence;

import goorm.humandelivery.driver.application.port.out.LoadTaxiDriverTypePort;
import goorm.humandelivery.driver.application.port.out.SaveTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.dto.response.TaxiTypeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaTaxiDriverRepository extends
        JpaRepository<TaxiDriver, Long>,
        SaveTaxiDriverPort,
        goorm.humandelivery.call.application.port.out.LoadTaxiDriverPort,
        goorm.humandelivery.driver.application.port.out.LoadTaxiDriverPort,
        LoadTaxiDriverTypePort {

    @Query("select t.id from TaxiDriver t where t.loginId = :loginId")
    Optional<Long> findIdByLoginId(String loginId);

    @Query("select new goorm.humandelivery.driver.dto.response.TaxiTypeResponse(x.taxiType) " +
            "from TaxiDriver t join t.taxi x " +
            "where t.loginId= :loginId")
    Optional<TaxiTypeResponse> findTaxiDriversTaxiTypeByLoginId(String loginId);

}
