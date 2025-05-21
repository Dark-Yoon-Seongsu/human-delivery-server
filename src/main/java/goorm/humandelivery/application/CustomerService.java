package goorm.humandelivery.application;

import goorm.humandelivery.customer.application.port.out.LoadCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerService {

    private final LoadCustomerPort loadCustomerPort;

    @Transactional(readOnly = true)
    public Customer findCustomerByLoginId(String loginId) {
        return loadCustomerPort.findByLoginId(loginId).orElseThrow(CustomerNotFoundException::new);
    }

}
