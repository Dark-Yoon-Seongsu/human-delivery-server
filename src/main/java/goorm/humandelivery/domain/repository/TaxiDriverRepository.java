package goorm.humandelivery.domain.repository;

import goorm.humandelivery.domain.model.response.TaxiTypeResponse;
import goorm.humandelivery.driver.domain.Taxi;
import goorm.humandelivery.driver.domain.TaxiDriver;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaxiDriverRepository extends JpaRepository<TaxiDriver, Long> {

    Optional<TaxiDriver> findByLoginId(String loginId);

    @Query("select t.id from TaxiDriver t where t.loginId = :loginId")
    Optional<Long> findIdByLoginId(String loginId);


    @Query("select new goorm.humandelivery.domain.model.response.TaxiTypeResponse(x.taxiType) " +
            "from TaxiDriver t join t.taxi x " +
            "where t.loginId= :loginId")
    Optional<TaxiTypeResponse> findTaxiDriversTaxiTypeByLoginId(String loginId);

    TaxiDriver taxi(Taxi taxi);

    boolean existsByLoginId(@Email @NotBlank String loginId);

}
