package goorm.humandelivery.customer.infrastructure.persistence;

import goorm.humandelivery.customer.application.port.out.LoadCustomerPort;
import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaCustomerRepository extends
        JpaRepository<Customer, Long>,
        LoadCustomerPort,
        SaveCustomerPort {

    @Query("SELECT c FROM Customer c WHERE c.loginId = :loginId")
    @Override
    Optional<Customer> findByLoginId(String loginId);

    @Query("SELECT c FROM Customer c WHERE c.id = :id")
    @Override
    Optional<Customer> findById(Long id);
}
