package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.in.LoginTaxiDriverUseCase;
import goorm.humandelivery.driver.application.port.out.LoadTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.dto.request.LoginTaxiDriverRequest;
import goorm.humandelivery.global.exception.IncorrectPasswordException;
import goorm.humandelivery.global.exception.TaxiDriverEntityNotFoundException;
import goorm.humandelivery.shared.dto.response.JwtResponse;
import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoginTaxiDriverService implements LoginTaxiDriverUseCase {

    private final LoadTaxiDriverPort loadTaxiDriverPort;
    private final JwtTokenProviderPort jwtTokenProviderPort;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public JwtResponse login(LoginTaxiDriverRequest loginTaxiDriverRequest) {
        TaxiDriver taxiDriver = loadTaxiDriverPort.findByLoginId(loginTaxiDriverRequest.getLoginId())
                .orElseThrow(TaxiDriverEntityNotFoundException::new);

        // 패스워드 검증
        if (!bCryptPasswordEncoder.matches(loginTaxiDriverRequest.getPassword(), taxiDriver.getPassword())) {
            throw new IncorrectPasswordException();
        }

        String token = jwtTokenProviderPort.generateToken(loginTaxiDriverRequest.getLoginId());

        return new JwtResponse(token);
    }
}
