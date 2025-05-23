package goorm.humandelivery.application.customer.controller;

import goorm.humandelivery.application.customer.service.CustomerLoadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goorm.humandelivery.domain.model.request.CreateCustomerRequest;
import goorm.humandelivery.domain.model.response.CreateCustomerResponse;
import goorm.humandelivery.domain.model.request.LoginCustomerRequest;
import goorm.humandelivery.domain.model.response.LoginCustomerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

	private final CustomerLoadService customerLoadService;

	// 회원가입
	@PostMapping
	public ResponseEntity<CreateCustomerResponse> register(@RequestBody @Valid CreateCustomerRequest createCustomerRequest) {
		log.info("승객 회원가입 요청 수신");
		CreateCustomerResponse response = customerLoadService.register(createCustomerRequest);
		log.info("신규 승객 회원 DB 저장 완료");
		return ResponseEntity.ok(response);
	}

	// 로그인
	@PostMapping("/auth-tokens")
	public ResponseEntity<LoginCustomerResponse> login(@RequestBody @Valid LoginCustomerRequest loginCustomerRequest) {
		log.info("승객 로그인 요청 수신");
		LoginCustomerResponse response = customerLoadService.authenticateAndGenerateToken(loginCustomerRequest);
		log.info("승객 토근 발급 완료");
		return ResponseEntity.ok(response);
	}

	// 회원정보수정

	// 회원 탈퇴

}
