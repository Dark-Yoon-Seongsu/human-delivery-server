package goorm.humandelivery.application;

import goorm.humandelivery.common.exception.CustomerNotFoundException;
import goorm.humandelivery.common.exception.DuplicateLoginIdException;
import goorm.humandelivery.common.exception.DuplicatePhoneNumberException;
import goorm.humandelivery.common.exception.IncorrectPasswordException;
import goorm.humandelivery.domain.model.request.CreateCustomerRequest;
import goorm.humandelivery.domain.model.request.LoginCustomerRequest;
import goorm.humandelivery.domain.model.response.CreateCustomerResponse;
import goorm.humandelivery.domain.model.response.LoginCustomerResponse;
import goorm.humandelivery.domain.repository.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @AfterEach
    void tearDown() {
        customerRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class RegisterTest {
        @Test
        @DisplayName("회원가입 정보를 받아 회원을 생성한다")
        void register() throws Exception {
            // Given
            CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest("test", "test", "test", "test");

            // When
            CreateCustomerResponse createCustomerResponse = customerService.register(createCustomerRequest);

            // Then
            assertThat(createCustomerResponse.getLoginId()).isNotNull();
        }

        @Test
        @DisplayName("중복된 아이디로 회원가입 하려는 경우 예외가 발생한다.")
        void registerWithDuplicateLoginId() throws Exception {
            // Given
            CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest("test", "test", "test", "test");
            CreateCustomerRequest createCustomerRequest2 = new CreateCustomerRequest("test", "test", "test", "test2");
            customerService.register(createCustomerRequest);

            // When
            // Then
            assertThatThrownBy(() -> customerService.register(createCustomerRequest2))
                    .isInstanceOf(DuplicateLoginIdException.class)
                    .hasMessage("이미 사용 중인 아이디입니다.");
        }

        @Test
        @DisplayName("중복된 전화번호로 회원가입 하려는 경우 예외가 발생한다.")
        void registerWithDuplicatePhoneNumber() throws Exception {
            // Given
            CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest("test", "test", "test", "test");
            CreateCustomerRequest createCustomerRequest2 = new CreateCustomerRequest("test2", "test", "test", "test");
            customerService.register(createCustomerRequest);

            // When
            // Then
            assertThatThrownBy(() -> customerService.register(createCustomerRequest2))
                    .isInstanceOf(DuplicatePhoneNumberException.class)
                    .hasMessage("이미 등록된 전화번호입니다.");
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {
        @Test
        @DisplayName("로그인에 성공하면 액세스 토큰이 반환된다.")
        void authenticateAndGenerateToken() throws Exception {
            // Given
            CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest("test", "test", "test", "test");
            customerService.register(createCustomerRequest);
            LoginCustomerRequest loginCustomerRequest = new LoginCustomerRequest("test", "test");
            // When
            LoginCustomerResponse loginCustomerResponse = customerService.authenticateAndGenerateToken(loginCustomerRequest);

            // Then
            assertThat(loginCustomerResponse.getAccessToken()).isNotNull();
            assertThat(loginCustomerResponse.getAccessToken()).isNotBlank();
        }

        @Test
        @DisplayName("존재하지 않는 아이디로 로그인하면 예외가 발생한다.")
        void authenticateAndGenerateTokenWithNoLoginId() throws Exception {
            // Given
            LoginCustomerRequest loginCustomerRequest = new LoginCustomerRequest("test", "test");

            // When
            // Then
            assertThatThrownBy(() -> customerService.authenticateAndGenerateToken(loginCustomerRequest))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("로그인 하려는 아이디의 비밀번호가 일치하지 않으면 예외가 발생한다.")
        void authenticateAndGenerateTokenWithNotCorrectPassword() throws Exception {
            // Given
            CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest("test", "test", "test", "test");
            customerService.register(createCustomerRequest);

            LoginCustomerRequest loginCustomerRequest = new LoginCustomerRequest("test", "test2");

            // When
            // Then
            assertThatThrownBy(() -> customerService.authenticateAndGenerateToken(loginCustomerRequest))
                    .isInstanceOf(IncorrectPasswordException.class)
                    .hasMessage("패스워드가 일치하지 않습니다.");
        }
    }
}