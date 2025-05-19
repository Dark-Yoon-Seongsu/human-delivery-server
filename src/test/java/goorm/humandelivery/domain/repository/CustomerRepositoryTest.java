package goorm.humandelivery.domain.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import goorm.humandelivery.customer.application.port.out.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import goorm.humandelivery.customer.domain.Customer;

@DataJpaTest
public class CustomerRepositoryTest {

	@Autowired
	CustomerRepository customerRepository;

	@BeforeEach
	void setUp(){
		Customer customer = Customer.builder()
			.loginId("testid")
			.password("password123123")
			.name("tester")
			.phoneNumber("010-1234-5678")
			.build();
		customerRepository.save(customer);
	}

	@Nested
	@DisplayName("로그인 ID로 회원 찾기")
	class FindByLoginIdTest{

		@Test
		@DisplayName("회원 조회에 성공하면 회원 객체를 반환한다.")
		void found_successfully() {
			// when
			Optional<Customer> customer = customerRepository.findByLoginId("testid");

			// then
			assertThat(customer).isPresent();
			Customer foundCustomer = customer.get();
			assertThat(foundCustomer.getLoginId()).isEqualTo("testid");
			assertThat(foundCustomer.getPassword()).isEqualTo("password123123");
			assertThat(foundCustomer.getName()).isEqualTo("tester");
			assertThat(foundCustomer.getPhoneNumber()).isEqualTo("010-1234-5678");
		}

		@Test
		@DisplayName("해당하는 회원이 없다면 Optional이 empty로 반환된다.")
		void not_found() {
			Optional<Customer> customer = customerRepository.findByLoginId("wrongid");
			assertThat(customer).isEmpty();
		}
	}

	@Nested
	@DisplayName("특정 로그인 ID를 갖는 회원이 존재하는지 확인")
	class ExistsByLoginIdTest{
		@Test
		@DisplayName("회원 조회에 성공하면 참을 반환한다.")
		void found_successfully() {
			boolean result = customerRepository.existsByLoginId("testid");
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("해당하는 회원이 없다면 거짓을 반환한다.")
		void not_found() {
			boolean result = customerRepository.existsByLoginId("wrongid");
			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("특정 전화번호를 갖는 회원이 존재하는지 확인")
	class ExistsByPhoneNumberTest{
		@Test
		@DisplayName("회원 조회에 성공하면 참을 반환한다.")
		void found_successfully() {
			boolean result = customerRepository.existsByPhoneNumber("010-1234-5678");
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("해당하는 회원이 없다면 거짓을 반환한다.")
		void not_found() {
			boolean result = customerRepository.existsByPhoneNumber("010-1234-5679");
			assertThat(result).isFalse();
		}
	}
}
