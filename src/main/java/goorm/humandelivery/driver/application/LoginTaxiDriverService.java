package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.in.LoginTaxiDriverUseCase;
import goorm.humandelivery.driver.application.port.out.LoadTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.dto.request.LoginTaxiDriverRequest;
import goorm.humandelivery.global.exception.IncorrectPasswordException;
import goorm.humandelivery.global.exception.JwtTokenGenerationException;
import goorm.humandelivery.global.exception.DriverEntityNotFoundException;
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
        String loginId = loginTaxiDriverRequest.getLoginId();
        String password = loginTaxiDriverRequest.getPassword();

        if (loginId == null || loginId.trim().isEmpty()) {
            throw new IllegalArgumentException("로그인 ID는 필수입니다.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        TaxiDriver taxiDriver = loadTaxiDriverPort.findByLoginId(loginId)
                .orElseThrow(DriverEntityNotFoundException::new);



        if (!bCryptPasswordEncoder.matches(password, taxiDriver.getPassword())) {
            throw new IncorrectPasswordException();
        }

        try {
            String token = jwtTokenProviderPort.generateToken(loginId);
            return new JwtResponse(token);
        } catch (Exception e) {
            throw new JwtTokenGenerationException("JWT 토큰 발급에 실패했습니다.", e);
        }
    }
}
