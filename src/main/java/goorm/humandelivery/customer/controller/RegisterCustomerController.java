package goorm.humandelivery.customer.controller;

import goorm.humandelivery.customer.application.port.in.RegisterCustomerUseCase;
import goorm.humandelivery.customer.dto.request.RegisterCustomerRequest;
import goorm.humandelivery.customer.dto.response.RegisterCustomerResponse;
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
public class RegisterCustomerController {

    private final RegisterCustomerUseCase registerCustomerUseCase;

    // 회원가입
    @PostMapping
    public ResponseEntity<RegisterCustomerResponse> register(@RequestBody @Valid RegisterCustomerRequest registerCustomerRequest) {
        log.info("승객 회원가입 요청 수신");
        RegisterCustomerResponse response = registerCustomerUseCase.register(registerCustomerRequest);
        log.info("신규 승객 회원 DB 저장 완료");
        return ResponseEntity.ok(response);
    }

}
