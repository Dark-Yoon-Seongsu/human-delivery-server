package goorm.humandelivery.customer.application;

import goorm.humandelivery.common.exception.IncorrectPasswordException;
import goorm.humandelivery.common.security.jwt.JwtUtil;
import goorm.humandelivery.customer.application.port.in.LoginCustomerUseCase;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.application.port.out.CustomerRepository;
import goorm.humandelivery.customer.exception.CustomerNotFoundException;
import goorm.humandelivery.customer.dto.request.LoginCustomerRequest;
import goorm.humandelivery.customer.dto.response.LoginCustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoginCustomerService implements LoginCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginCustomerResponse authenticateAndGenerateToken(LoginCustomerRequest loginCustomerRequest) {
        Customer customer = customerRepository.findByLoginId(loginCustomerRequest.getLoginId())
                .orElseThrow(CustomerNotFoundException::new);

        if (!bCryptPasswordEncoder.matches(loginCustomerRequest.getPassword(), customer.getPassword())) {
            throw new IncorrectPasswordException();
        }

        return new LoginCustomerResponse(jwtUtil.generateToken(customer.getLoginId()));
    }
}
