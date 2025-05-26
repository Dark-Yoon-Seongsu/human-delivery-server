package goorm.humandelivery.customer.application.port.in;

import goorm.humandelivery.customer.domain.Customer;

public interface LoadCustomerUseCase {

    Customer findByLoginId(String customerLoginId);

}
