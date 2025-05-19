package goorm.humandelivery.customer.application.port.out;

import goorm.humandelivery.customer.domain.Customer;

public interface SaveCustomerPort {

    Customer save(Customer customer);

    boolean existsByLoginId(String loginId);

    boolean existsByPhoneNumber(String phoneNumber);

    void deleteAllInBatch();

}
