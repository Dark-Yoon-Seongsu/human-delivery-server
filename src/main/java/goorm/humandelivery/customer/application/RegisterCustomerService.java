package goorm.humandelivery.customer.application;

import goorm.humandelivery.customer.exception.DuplicateLoginIdException;
import goorm.humandelivery.customer.exception.DuplicatePhoneNumberException;
import goorm.humandelivery.customer.application.port.in.RegisterCustomerUseCase;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.application.port.out.CustomerRepository;
import goorm.humandelivery.customer.dto.request.RegisterCustomerRequest;
import goorm.humandelivery.customer.dto.response.RegisterCustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterCustomerService implements RegisterCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public RegisterCustomerResponse register(RegisterCustomerRequest registerCustomerRequest) {

        if (customerRepository.existsByLoginId(registerCustomerRequest.getLoginId())) {
            throw new DuplicateLoginIdException();
        }

        if (customerRepository.existsByPhoneNumber(registerCustomerRequest.getPhoneNumber())) {
            throw new DuplicatePhoneNumberException();
        }

        Customer customer = Customer.builder()
                .loginId(registerCustomerRequest.getLoginId())
                .password(bCryptPasswordEncoder.encode(registerCustomerRequest.getPassword()))
                .name(registerCustomerRequest.getName())
                .phoneNumber(registerCustomerRequest.getPhoneNumber())
                .build();

        return new RegisterCustomerResponse(customerRepository.save(customer).getLoginId());
    }
}