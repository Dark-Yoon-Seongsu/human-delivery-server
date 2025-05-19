package goorm.humandelivery.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import goorm.humandelivery.TestFixture.CustomerTestFixture;
import goorm.humandelivery.common.exception.CustomerNotFoundException;
import goorm.humandelivery.common.exception.DuplicateLoginIdException;
import goorm.humandelivery.common.exception.DuplicatePhoneNumberException;
import goorm.humandelivery.common.exception.IncorrectPasswordException;
import goorm.humandelivery.common.security.jwt.JwtUtil;
import goorm.humandelivery.domain.model.entity.Customer;
import goorm.humandelivery.domain.model.request.CreateCustomerRequest;
import goorm.humandelivery.domain.model.request.LoginCustomerRequest;
import goorm.humandelivery.domain.model.response.CreateCustomerResponse;
import goorm.humandelivery.domain.model.response.LoginCustomerResponse;
import goorm.humandelivery.domain.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
public class OldCustomerServiceTest {

	@Mock CustomerRepository customerRepository;
	@Mock BCryptPasswordEncoder bCryptPasswordEncoder;
	@Mock JwtUtil jwtUtil;
	@InjectMocks
	CustomerService customerService;

	CreateCustomerRequest newCustomerDTO;
	LoginCustomerRequest loginCustomerRequest;
	Customer newCustomer;

	@BeforeEach
	void setUp(){
		newCustomerDTO = CustomerTestFixture.createCreateCustomerRequest(
			"registered_customer",
			"registered_customer_password_1234",
			"John Doe",
			"010-1234-5678");

		loginCustomerRequest = CustomerTestFixture.createLoginCustomerRequest(
			"registered_customer",
			"registered_customer_password_1234");

		newCustomer = CustomerTestFixture.createCustomerEntity(
			newCustomerDTO.getLoginId(),
			"Encrypted password",
			newCustomerDTO.getName(),
			newCustomerDTO.getPhoneNumber());
	}

	@Nested
	@DisplayName("회원가입 테스트")
	class RegisterTest {
		@Test
		@DisplayName("성공 시 로그인 ID를 담은 응답 DTO가 반환된다.")
		void success(){
			// given
			when(customerRepository.save(any(Customer.class)))
				.thenReturn(newCustomer);
			when(bCryptPasswordEncoder.encode(anyString())).thenReturn("Encrypted password");

			// when
			CreateCustomerResponse createCustomerResponse = customerService.register(newCustomerDTO);

			// then
			assertThat(createCustomerResponse.getLoginId()).isEqualTo("registered_customer");
		}

		@Test
		@DisplayName("이미 가입된 아이디로 시도할 경우 DuplicateLoginIdException 예외가 발생한다.")
		void fail_duplicate_login_id(){
			// given
			when(customerRepository.existsByLoginId(anyString())).thenReturn(true);

			// when & then
			assertThrows(DuplicateLoginIdException.class, () -> customerService.register(newCustomerDTO));
		}

		@Test
		@DisplayName("이미 가입된 전화번호로 시도할 경우 DuplicatePhoneNumberException 예외가 발생한다.")
		void fail_duplicate_phone_number(){
			// given
			when(customerRepository.existsByPhoneNumber(anyString())).thenReturn(true);

			// when & then
			assertThrows(DuplicatePhoneNumberException.class, () -> customerService.register(newCustomerDTO));
		}
	}

	@Nested
	@DisplayName("로그인 테스트")
	class AuthenticateAndGenerateTokenTest {
		@Test
		@DisplayName("성공 시 토큰을 담은 응답 DTO가 반환된다.")
		void success(){
			// given
			when(customerRepository.findByLoginId(anyString())).thenReturn(Optional.of(newCustomer));
			when(bCryptPasswordEncoder.matches(any(CharSequence.class), anyString())).thenReturn(true);
			when(jwtUtil.generateToken(anyString())).thenReturn("AccessToken");

			// when
			LoginCustomerResponse loginCustomerResponse =
				customerService.authenticateAndGenerateToken(
					new LoginCustomerRequest(
						"registered_customer", "registered_customer_password_1234"
					)
				);

			// then
			assertThat(loginCustomerResponse.getAccessToken()).isNotNull();
			assertThat(loginCustomerResponse.getAccessToken()).isEqualTo("AccessToken");
		}

		@Test
		@DisplayName("존재하지 않는 ID로 로그인을 시도할 경우 CustomerNotFoundException 예외가 발생한다.")
		void fail_customer_not_found(){
			// given
			when(customerRepository.findByLoginId(anyString())).thenReturn(Optional.empty());

			// when & then
			assertThrows(CustomerNotFoundException.class,
				() -> customerService.authenticateAndGenerateToken(loginCustomerRequest));
		}

		@Test
		@DisplayName("비밀번호가 일치하지 않을 경우 IncorrectPasswordException 예외가 발생한다.")
		void fail_incorrect_password(){
			// given
			when(customerRepository.findByLoginId(anyString())).thenReturn(Optional.of(newCustomer));
			when(bCryptPasswordEncoder.matches(any(CharSequence.class), anyString()))
				.thenReturn(false);

			// when & then
			assertThrows(IncorrectPasswordException.class,
				() -> customerService.authenticateAndGenerateToken(loginCustomerRequest));
		}
	}
}
