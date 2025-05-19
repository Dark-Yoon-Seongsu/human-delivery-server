package goorm.humandelivery.customer.application;

import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
import goorm.humandelivery.global.exception.DuplicateLoginIdException;
import goorm.humandelivery.customer.exception.DuplicatePhoneNumberException;
import goorm.humandelivery.customer.dto.request.RegisterCustomerRequest;
import goorm.humandelivery.customer.dto.response.RegisterCustomerResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class RegisterCustomerServiceTest {

    @Autowired
    private RegisterCustomerService registerCustomerService;

    @Autowired
    private SaveCustomerPort saveCustomerPort;

    @AfterEach
    void tearDown() {
        saveCustomerPort.deleteAllInBatch();
    }

    @Nested
    @DisplayName("승객 회원가입 테스트")
    class RegisterTest {
        @Test
        @DisplayName("회원가입 정보를 받아 회원을 생성한다")
        void register() throws Exception {
            // Given
            RegisterCustomerRequest registerCustomerRequest = new RegisterCustomerRequest("test", "test", "test", "test");

            // When
            RegisterCustomerResponse registerCustomerResponse = registerCustomerService.register(registerCustomerRequest);

            // Then
            assertThat(registerCustomerResponse.getLoginId()).isNotNull();
        }

        @Test
        @DisplayName("중복된 아이디로 회원가입 하려는 경우 예외가 발생한다.")
        void registerWithDuplicateLoginId() throws Exception {
            // Given
            RegisterCustomerRequest registerCustomerRequest = new RegisterCustomerRequest("test", "test", "test", "test");
            RegisterCustomerRequest registerCustomerRequest2 = new RegisterCustomerRequest("test", "test", "test", "test2");
            registerCustomerService.register(registerCustomerRequest);

            // When
            // Then
            assertThatThrownBy(() -> registerCustomerService.register(registerCustomerRequest2))
                    .isInstanceOf(DuplicateLoginIdException.class)
                    .hasMessage("이미 사용 중인 아이디입니다.");
        }

        @Test
        @DisplayName("중복된 전화번호로 회원가입 하려는 경우 예외가 발생한다.")
        void registerWithDuplicatePhoneNumber() throws Exception {
            // Given
            RegisterCustomerRequest registerCustomerRequest = new RegisterCustomerRequest("test", "test", "test", "test");
            RegisterCustomerRequest registerCustomerRequest2 = new RegisterCustomerRequest("test2", "test", "test", "test");
            registerCustomerService.register(registerCustomerRequest);

            // When
            // Then
            assertThatThrownBy(() -> registerCustomerService.register(registerCustomerRequest2))
                    .isInstanceOf(DuplicatePhoneNumberException.class)
                    .hasMessage("이미 등록된 전화번호입니다.");
        }
    }
}