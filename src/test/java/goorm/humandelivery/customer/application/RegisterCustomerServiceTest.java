package goorm.humandelivery.customer.application;

import goorm.humandelivery.customer.application.port.out.LoadCustomerPort;
import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.dto.request.RegisterCustomerRequest;
import goorm.humandelivery.customer.dto.response.RegisterCustomerResponse;
import goorm.humandelivery.customer.exception.DuplicatePhoneNumberException;
import goorm.humandelivery.global.exception.DuplicateLoginIdException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@SpringBootTest
public class RegisterCustomerServiceTest {

    @Autowired
    private RegisterCustomerService registerCustomerService;

    @Autowired
    private SaveCustomerPort saveCustomerPort;
    
    @Autowired
    private LoadCustomerPort loadCustomerPort;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private RegisterCustomerRequest registerCustomerRequest;

    @AfterEach
    void tearDown() {
        saveCustomerPort.deleteAllInBatch();
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class RegisterTest {
        
        @Test
        @DisplayName("정상적인 회원가입이 성공한다.")
        void register_Success() {
            // Given
            registerCustomerRequest = new RegisterCustomerRequest(
                "testuser", "password123", "홍길동", "010-1234-5678"
            );

            // When
            RegisterCustomerResponse response = registerCustomerService.register(registerCustomerRequest);

            // Then
            assertThat(response.getLoginId()).isEqualTo("testuser");
            assertThat(saveCustomerPort.existsByLoginId("testuser")).isTrue();
        }

        @Test
        @DisplayName("중복된 로그인 아이디로 회원가입하면 예외가 발생한다.")
        void register_DuplicateLoginId_ThrowsException() {
            // Given
            registerCustomerRequest = new RegisterCustomerRequest(
                "testuser", "password123", "홍길동", "010-1234-5678"
            );
            registerCustomerService.register(registerCustomerRequest);

            RegisterCustomerRequest duplicateRequest = new RegisterCustomerRequest(
                "testuser", "password456", "김철수", "010-9876-5432"
            );

            // When & Then
            assertThatThrownBy(() -> registerCustomerService.register(duplicateRequest))
                    .isInstanceOf(DuplicateLoginIdException.class);
        }

        @Test
        @DisplayName("중복된 전화번호로 회원가입하면 예외가 발생한다.")
        void register_DuplicatePhoneNumber_ThrowsException() {
            // Given
            registerCustomerRequest = new RegisterCustomerRequest(
                "testuser1", "password123", "홍길동", "010-1234-5678"
            );
            registerCustomerService.register(registerCustomerRequest);

            RegisterCustomerRequest duplicateRequest = new RegisterCustomerRequest(
                "testuser2", "password456", "김철수", "010-1234-5678"
            );

            // When & Then
            assertThatThrownBy(() -> registerCustomerService.register(duplicateRequest))
                    .isInstanceOf(DuplicatePhoneNumberException.class);
        }

        @Test
        @DisplayName("비밀번호가 암호화되어 저장된다.")
        void register_PasswordEncrypted() {
            // Given
            String rawPassword = "password123";
            registerCustomerRequest = new RegisterCustomerRequest(
                "testuser", rawPassword, "홍길동", "010-1234-5678"
            );

            // When
            registerCustomerService.register(registerCustomerRequest);

            // Then
            var savedCustomer = loadCustomerPort.findByLoginId("testuser").orElseThrow();
            assertThat(savedCustomer.getPassword()).isNotEqualTo(rawPassword);
            assertThat(savedCustomer.getPassword()).startsWith("$2a$"); // BCrypt 해시 확인
        }
    }
}