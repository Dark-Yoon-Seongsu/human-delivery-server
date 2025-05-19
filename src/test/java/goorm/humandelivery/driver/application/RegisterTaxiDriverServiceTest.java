package goorm.humandelivery.driver.application;

import goorm.humandelivery.global.exception.DuplicateLoginIdException;
import goorm.humandelivery.driver.application.port.out.SaveTaxiDriverPort;
import goorm.humandelivery.driver.application.port.out.SaveTaxiPort;
import goorm.humandelivery.driver.dto.request.RegisterTaxiDriverRequest;
import goorm.humandelivery.driver.dto.request.RegisterTaxiRequest;
import goorm.humandelivery.driver.dto.response.RegisterTaxiDriverResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class RegisterTaxiDriverServiceTest {

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
    @DisplayName("택시기사 회원가입 테스트")
    class RegisterTest {

        @Test
        @DisplayName("회원가입 정보를 받아 회원을 생성한다.")
        void register() {
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

            // When
            RegisterTaxiDriverResponse response = registerTaxiDriverService.register(request);

            // Then
            assertThat(response.getLoginId()).isEqualTo("driver1@email.com");
            assertThat(response.getName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("중복된 아이디로 회원가입 하려는 경우 예외가 발생한다")
        void registerWithDuplicateLoginId() {
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

            // When
            // Then
            assertThatThrownBy(() -> registerTaxiDriverService.register(request))
                    .isInstanceOf(DuplicateLoginIdException.class)
                    .hasMessage("이미 사용 중인 아이디입니다.");
        }
    }
}
