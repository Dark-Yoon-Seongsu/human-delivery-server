package goorm.humandelivery.customer.controller;

import goorm.humandelivery.customer.application.port.in.LoginCustomerUseCase;
import goorm.humandelivery.customer.dto.request.LoginCustomerRequest;
import goorm.humandelivery.customer.dto.response.LoginCustomerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@Slf4j
public class LoginCustomerController {

    private final LoginCustomerUseCase loginCustomerUseCase;

    // 로그인
    @PostMapping("/auth-tokens")
    public ResponseEntity<LoginCustomerResponse> login(@RequestBody @Valid LoginCustomerRequest loginCustomerRequest) {
        log.info("승객 로그인 요청 수신");
        LoginCustomerResponse response = loginCustomerUseCase.authenticateAndGenerateToken(loginCustomerRequest);
        log.info("승객 토근 발급 완료");
        return ResponseEntity.ok(response);
    }
}
