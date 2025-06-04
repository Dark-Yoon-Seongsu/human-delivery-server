package goorm.humandelivery.driver.controller;

import goorm.humandelivery.driver.application.port.in.RegisterTaxiDriverUseCase;
import goorm.humandelivery.driver.dto.request.RegisterTaxiDriverRequest;
import goorm.humandelivery.driver.dto.response.RegisterTaxiDriverResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/taxi-driver")
@RequiredArgsConstructor
public class RegisterTaxiDriverController {

    private final RegisterTaxiDriverUseCase registerTaxiDriverUseCase;

    // 회원가입
    @PostMapping
    public ResponseEntity<?> register(@RequestBody @Valid RegisterTaxiDriverRequest taxiDriverRequest) {
        log.info("택시기사 회원가입 요청 수신");
        RegisterTaxiDriverResponse response = registerTaxiDriverUseCase.register(taxiDriverRequest);
        log.info("신규 택시기사 DB 저장 완료");
        return ResponseEntity.ok(response);
    }

}
