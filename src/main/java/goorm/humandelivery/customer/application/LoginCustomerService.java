package goorm.humandelivery.customer.application;

import goorm.humandelivery.customer.application.port.in.LoginCustomerUseCase;
import goorm.humandelivery.customer.application.port.out.LoadCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.dto.request.LoginCustomerRequest;
import goorm.humandelivery.customer.dto.response.LoginCustomerResponse;
import goorm.humandelivery.customer.exception.CustomerNotFoundException;
import goorm.humandelivery.global.exception.IncorrectPasswordException;
import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoginCustomerService implements LoginCustomerUseCase {

    private final LoadCustomerPort loadCustomerPort;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProviderPort jwtTokenProviderPort;

    @Override
    public LoginCustomerResponse authenticateAndGenerateToken(LoginCustomerRequest loginCustomerRequest) {
        Customer customer = loadCustomerPort.findByLoginId(loginCustomerRequest.getLoginId())
                .orElseThrow(CustomerNotFoundException::new);

        if (!bCryptPasswordEncoder.matches(loginCustomerRequest.getPassword(), customer.getPassword())) {
            throw new IncorrectPasswordException();
        }

        return new LoginCustomerResponse(jwtTokenProviderPort.generateToken(customer.getLoginId()));
    }
}
