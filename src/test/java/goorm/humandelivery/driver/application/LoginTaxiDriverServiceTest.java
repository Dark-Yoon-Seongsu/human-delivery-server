package goorm.humandelivery.driver.application;

import goorm.humandelivery.shared.dto.response.JwtResponse;
import goorm.humandelivery.global.exception.IncorrectPasswordException;
import goorm.humandelivery.global.exception.TaxiDriverEntityNotFoundException;
import goorm.humandelivery.driver.application.port.out.SaveTaxiDriverPort;
import goorm.humandelivery.driver.application.port.out.SaveTaxiPort;
import goorm.humandelivery.driver.dto.request.LoginTaxiDriverRequest;
import goorm.humandelivery.driver.dto.request.RegisterTaxiDriverRequest;
import goorm.humandelivery.driver.dto.request.RegisterTaxiRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class LoginTaxiDriverServiceTest {

    @Autowired
    private LoginTaxiDriverService loginTaxiDriverService;

    @Autowired
    private RegisterTaxiDriverService registerTaxiDriverService;

    @Autowired
    private SaveTaxiDriverPort saveTaxiDriverPort;

    @Autowired
    private SaveTaxiPort saveTaxiPort;

    @AfterEach
    void tearDown() {
        saveTaxiDriverPort.deleteAllInBatch();
        saveTaxiPort.deleteAllInBatch();
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("로그인에 성공하면 JWT 토큰이 반환된다.")
        void loginSuccess() {
            // Given
            RegisterTaxiRequest registerTaxiRequest = new RegisterTaxiRequest();
            registerTaxiRequest.setModel("Sonata");
            registerTaxiRequest.setTaxiType("NORMAL");
            registerTaxiRequest.setFuelType("GASOLINE");
            registerTaxiRequest.setPlateNumber("12가1234");

            RegisterTaxiDriverRequest request = new RegisterTaxiDriverRequest();
            request.setLoginId("driver1@email.com");
            request.setPassword("1234");
            request.setName("홍길동");
            request.setPhoneNumber("010-1234-5678");
            request.setLicenseCode("LIC123456");
            request.setTaxi(registerTaxiRequest);

            registerTaxiDriverService.register(request);

            LoginTaxiDriverRequest loginRequest = new LoginTaxiDriverRequest();
            loginRequest.setLoginId("driver1@email.com");
            loginRequest.setPassword("1234");

            // When
            JwtResponse response = loginTaxiDriverService.login(loginRequest);

            // Then
            assertThat(response.getToken()).isNotNull();
            assertThat(response.getToken()).isNotBlank();
        }

        @Test
        @DisplayName("존재하지 않는 아이디로 로그인하면 예외가 발생한다.")
        void loginWithInvalidLoginId() {
            // Given
            LoginTaxiDriverRequest loginRequest = new LoginTaxiDriverRequest();
            loginRequest.setLoginId("driver1@email.com");
            loginRequest.setPassword("1234");

            // When
            // Then
            assertThatThrownBy(() -> loginTaxiDriverService.login(loginRequest))
                    .isInstanceOf(TaxiDriverEntityNotFoundException.class)
                    .hasMessage("아이디에 해당하는 TaxiDriver 엔티티가 존재하지 않습니다.");
        }

        @Test
        @DisplayName("비밀번호가 틀린 경우 예외가 발생한다.")
        void loginWithIncorrectPassword() {
            // Given
            RegisterTaxiRequest registerTaxiRequest = new RegisterTaxiRequest();
            registerTaxiRequest.setModel("Sonata");
            registerTaxiRequest.setTaxiType("NORMAL");
            registerTaxiRequest.setFuelType("GASOLINE");
            registerTaxiRequest.setPlateNumber("12가1234");

            RegisterTaxiDriverRequest request = new RegisterTaxiDriverRequest();
            request.setLoginId("driver1@email.com");
            request.setPassword("1234");
            request.setName("홍길동");
            request.setPhoneNumber("010-1234-5678");
            request.setLicenseCode("LIC123456");
            request.setTaxi(registerTaxiRequest);

            registerTaxiDriverService.register(request);

            LoginTaxiDriverRequest loginRequest = new LoginTaxiDriverRequest();
            loginRequest.setLoginId("driver1@email.com");
            loginRequest.setPassword("0000");

            // When
            // Then
            assertThatThrownBy(() -> loginTaxiDriverService.login(loginRequest))
                    .isInstanceOf(IncorrectPasswordException.class)
                    .hasMessage("패스워드가 일치하지 않습니다.");
        }
    }
}
