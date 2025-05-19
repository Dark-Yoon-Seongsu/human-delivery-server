package goorm.humandelivery.customer.infrastructure.persistence;

import goorm.humandelivery.customer.application.port.out.LoadCustomerPort;
import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCustomerRepository extends
        JpaRepository<Customer, Long>,
        LoadCustomerPort,
        SaveCustomerPort {
}
