package goorm.humandelivery.driver.controller;

import goorm.humandelivery.driver.application.port.in.RegisterTaxiDriverUseCase;
import goorm.humandelivery.driver.dto.request.RegisterTaxiDriverRequest;
import goorm.humandelivery.driver.dto.response.RegisterTaxiDriverResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/taxi-driver")
@RequiredArgsConstructor
public class RegisterTaxiDriverController {

    private final RegisterTaxiDriverUseCase registerTaxiDriverUseCase;

    // 회원가입
    @PostMapping
    public ResponseEntity<?> register(@RequestBody @Valid RegisterTaxiDriverRequest taxiDriverRequest,
                                      BindingResult bindingResult) {

        // 밸리데이션 응답 추가.
        if (bindingResult.hasErrors()) {
            List<String> fieldErrors = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(Map.of("errors", fieldErrors));
        }

        RegisterTaxiDriverResponse response = registerTaxiDriverUseCase.register(taxiDriverRequest);
        return ResponseEntity.ok(response);
    }

}
