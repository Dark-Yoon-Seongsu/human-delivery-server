package goorm.humandelivery.customer.infrastructure.persistence;

import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.application.port.out.CustomerRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCustomerRepository extends CustomerRepository, JpaRepository<Customer, Long> {
}
