package goorm.humandelivery.fare.controller;

import goorm.humandelivery.fare.application.port.in.EstimateFareUseCase;
import goorm.humandelivery.fare.dto.request.EstimateFareRequest;
import goorm.humandelivery.fare.dto.response.EstimateFareResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/estimate")
@RequiredArgsConstructor
public class EstimateFareController {

    private final EstimateFareUseCase estimateFareUseCase;

    @PostMapping
    public ResponseEntity<EstimateFareResponse> estimateFare(@RequestBody @Valid EstimateFareRequest estimateFareRequest) {
        log.info("[EstimateFareController.estimateFare] 예상 요금 확인 호출");
        return ResponseEntity.ok(estimateFareUseCase.estimateFare(estimateFareRequest, LocalTime.now()));
    }
}
