package goorm.humandelivery.customer.application.port.out;

import goorm.humandelivery.customer.domain.Customer;

import java.util.Optional;

public interface LoadCustomerPort {

    Optional<Customer> findByLoginId(String loginId);

}
