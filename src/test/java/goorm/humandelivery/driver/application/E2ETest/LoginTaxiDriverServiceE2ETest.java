package goorm.humandelivery.driver.application.E2ETest;

import goorm.humandelivery.driver.application.LoginTaxiDriverService;
import goorm.humandelivery.driver.application.port.out.SaveTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.dto.request.LoginTaxiDriverRequest;
import goorm.humandelivery.global.exception.IncorrectPasswordException;
import goorm.humandelivery.global.exception.DriverEntityNotFoundException;
import goorm.humandelivery.shared.dto.response.JwtResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

//./gradlew test --tests "*LoginTaxiDriverServiceE2ETest"
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class LoginTaxiDriverServiceE2ETest {

    @Autowired
    private LoginTaxiDriverService loginTaxiDriverService;

    @Autowired
    private SaveTaxiDriverPort saveTaxiDriverPort;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final String validLoginId = "testdriver@example.com";
    private final String rawPassword = "password123";

    @BeforeEach
    void setUp() {
        TaxiDriver driver = TaxiDriver.builder()
                .loginId(validLoginId)
                .password(passwordEncoder.encode(rawPassword))
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();
        saveTaxiDriverPort.save(driver);
    }

    @Test
    @DisplayName("1. 로그인 성공")
    void loginSuccess() {
        JwtResponse response = loginTaxiDriverService.login(
                LoginTaxiDriverRequest.builder()
                        .loginId(validLoginId)
                        .password(rawPassword)
                        .build());

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("2. 로그인 실패 - 존재하지 않는 ID")
    void loginFailNotFound() {
        LoginTaxiDriverRequest request = LoginTaxiDriverRequest.builder()
                .loginId("nonexistent")
                .password(rawPassword)
                .build();

        assertThatThrownBy(() -> loginTaxiDriverService.login(request))
                .isInstanceOf(DriverEntityNotFoundException.class);
    }

    @Test
    @DisplayName("3. 로그인 실패 - 비밀번호 불일치")
    void loginFailWrongPassword() {
        LoginTaxiDriverRequest request = LoginTaxiDriverRequest.builder()
                .loginId(validLoginId)
                .password("wrongpassword")
                .build();

        assertThatThrownBy(() -> loginTaxiDriverService.login(request))
                .isInstanceOf(IncorrectPasswordException.class);
    }


}

