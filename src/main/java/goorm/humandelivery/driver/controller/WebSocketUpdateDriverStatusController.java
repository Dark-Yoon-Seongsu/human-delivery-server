package goorm.humandelivery.driver.controller;

import goorm.humandelivery.driver.application.port.in.UpdateDriverStatusUseCase;
import goorm.humandelivery.driver.dto.request.UpdateTaxiDriverStatusRequest;
import goorm.humandelivery.driver.dto.response.UpdateTaxiDriverStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;

@Controller
@MessageMapping("/taxi-driver")  // "/app/taxi-driver"
@RequiredArgsConstructor
public class WebSocketUpdateDriverStatusController {

    private final UpdateDriverStatusUseCase updateDriverStatusUseCase;

    @MessageMapping("/update-status")
    @SendToUser("/queue/taxi-driver-status")
    public UpdateTaxiDriverStatusResponse updateStatus(UpdateTaxiDriverStatusRequest request, Principal principal) {
        return updateDriverStatusUseCase.updateStatus(request, principal.getName());
    }
}
