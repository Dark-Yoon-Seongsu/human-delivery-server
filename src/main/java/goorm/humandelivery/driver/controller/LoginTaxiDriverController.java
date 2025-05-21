package goorm.humandelivery.driver.controller;

import goorm.humandelivery.driver.application.port.in.LoginTaxiDriverUseCase;
import goorm.humandelivery.driver.dto.request.LoginTaxiDriverRequest;
import goorm.humandelivery.shared.dto.response.JwtResponse;
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
public class LoginTaxiDriverController {

    private final LoginTaxiDriverUseCase loginTaxiDriverUseCase;

    // 로그인
    @PostMapping("/auth-tokens")
    public ResponseEntity<?> loginTaxiDriver(@RequestBody @Valid LoginTaxiDriverRequest loginTaxiDriverRequest) {
        JwtResponse jwtResponse = loginTaxiDriverUseCase.login(loginTaxiDriverRequest);
        return ResponseEntity.ok(jwtResponse);
    }

}
