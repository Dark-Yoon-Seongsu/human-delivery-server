package goorm.humandelivery.customer.application.port.out;

import goorm.humandelivery.customer.domain.Customer;

import java.util.Optional;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByPhoneNumber(String phoneNumber);

    void deleteAllInBatch();
}
