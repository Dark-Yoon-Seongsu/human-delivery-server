//package goorm.humandelivery.customer.application;
//
//import goorm.humandelivery.global.exception.IncorrectPasswordException;
//import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
//import goorm.humandelivery.customer.dto.request.LoginCustomerRequest;
//import goorm.humandelivery.customer.dto.request.RegisterCustomerRequest;
//import goorm.humandelivery.customer.dto.response.LoginCustomerResponse;
//import goorm.humandelivery.customer.exception.CustomerNotFoundException;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@SpringBootTest
//public class LoginCustomerServiceTest {
//
//    @Autowired
//    private LoginCustomerService loginCustomerService;
//
//    @Autowired
//    private RegisterCustomerService registerCustomerService;
//
//    @Autowired
//    private SaveCustomerPort saveCustomerPort;
//
//    @AfterEach
//    void tearDown() {
//        saveCustomerPort.deleteAllInBatch();
//    }
//
//    @Nested
//    @DisplayName("로그인 테스트")
//    class LoginTest {
//        @Test
//        @DisplayName("로그인에 성공하면 액세스 토큰이 반환된다.")
//        void authenticateAndGenerateToken() throws Exception {
//            // Given
//            RegisterCustomerRequest registerCustomerRequest = new RegisterCustomerRequest("test", "test", "test", "test");
//            registerCustomerService.register(registerCustomerRequest);
//            LoginCustomerRequest loginCustomerRequest = new LoginCustomerRequest("test", "test");
//            // When
//            LoginCustomerResponse loginCustomerResponse = loginCustomerService.authenticateAndGenerateToken(loginCustomerRequest);
//
//            // Then
//            assertThat(loginCustomerResponse.getAccessToken()).isNotNull();
//            assertThat(loginCustomerResponse.getAccessToken()).isNotBlank();
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 아이디로 로그인하면 예외가 발생한다.")
//        void authenticateAndGenerateTokenWithNoLoginId() throws Exception {
//            // Given
//            LoginCustomerRequest loginCustomerRequest = new LoginCustomerRequest("test", "test");
//
//            // When
//            // Then
//            assertThatThrownBy(() -> loginCustomerService.authenticateAndGenerateToken(loginCustomerRequest))
//                    .isInstanceOf(CustomerNotFoundException.class)
//                    .hasMessage("사용자를 찾을 수 없습니다.");
//        }
//
//        @Test
//        @DisplayName("로그인 하려는 아이디의 비밀번호가 일치하지 않으면 예외가 발생한다.")
//        void authenticateAndGenerateTokenWithNotCorrectPassword() throws Exception {
//            // Given
//            RegisterCustomerRequest registerCustomerRequest = new RegisterCustomerRequest("test", "test", "test", "test");
//            registerCustomerService.register(registerCustomerRequest);
//
//            LoginCustomerRequest loginCustomerRequest = new LoginCustomerRequest("test", "test2");
//
//            // When
//            // Then
//            assertThatThrownBy(() -> loginCustomerService.authenticateAndGenerateToken(loginCustomerRequest))
//                    .isInstanceOf(IncorrectPasswordException.class)
//                    .hasMessage("패스워드가 일치하지 않습니다.");
//        }
//    }
//}
