package goorm.humandelivery.customer.application;

import goorm.humandelivery.customer.application.port.in.LoadCustomerUseCase;
import goorm.humandelivery.customer.application.port.out.LoadCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoadCustomerService implements LoadCustomerUseCase {

    private final LoadCustomerPort loadCustomerPort;

    @Override
    public Customer findByLoginId(String customerLoginId) {
        return loadCustomerPort.findByLoginId(customerLoginId)
                .orElseThrow(CustomerNotFoundException::new);
    }
}
