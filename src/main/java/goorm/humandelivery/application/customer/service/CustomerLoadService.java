package goorm.humandelivery.application.customer.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerLoadService {

	private final CustomerRepository customerRepository;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final JwtUtil jwtUtil;

	@Transactional
	public CreateCustomerResponse register(CreateCustomerRequest createCustomerRequest) {

		// 중복 검증
		if (customerRepository.existsByLoginId(createCustomerRequest.getLoginId())) {
			throw new DuplicateLoginIdException();
		}

		if (customerRepository.existsByPhoneNumber(createCustomerRequest.getPhoneNumber())) {
			throw new DuplicatePhoneNumberException();
		}

		Customer customer = Customer.builder()
			.loginId(createCustomerRequest.getLoginId())
			.password(bCryptPasswordEncoder.encode(createCustomerRequest.getPassword()))
			.name(createCustomerRequest.getName())
			.phoneNumber(createCustomerRequest.getPhoneNumber())
			.build();

		return new CreateCustomerResponse(customerRepository.save(customer).getLoginId());
	}

	@Transactional(readOnly = true)
	public LoginCustomerResponse authenticateAndGenerateToken(LoginCustomerRequest loginCustomerRequest) {
		Customer customer = customerRepository.findByLoginId(loginCustomerRequest.getLoginId())
			.orElseThrow(CustomerNotFoundException::new);

		if (!bCryptPasswordEncoder.matches(loginCustomerRequest.getPassword(), customer.getPassword())) {
			throw new IncorrectPasswordException();
		}

		return new LoginCustomerResponse(jwtUtil.generateToken(customer.getLoginId()));
	}

	@Transactional(readOnly = true)
	public Customer findCustomerByLoginId(String loginId) {
		return customerRepository.findByLoginId(loginId).orElseThrow(CustomerNotFoundException::new);
	}

}
