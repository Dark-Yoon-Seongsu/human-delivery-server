package goorm.humandelivery.driver.controller;

import goorm.humandelivery.driver.application.port.in.UpdateDriverLocationUseCase;
import goorm.humandelivery.driver.dto.request.UpdateDriverLocationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@MessageMapping("/taxi-driver")  // "/app/taxi-driver"
@RequiredArgsConstructor
public class WebSocketUpdateDriverLocationController {

    private final UpdateDriverLocationUseCase updateDriverLocationUseCase;

    @MessageMapping("/update-location")
    public void updateLocation(@Valid UpdateDriverLocationRequest request, Principal principal) {
        updateDriverLocationUseCase.updateLocation(request, principal.getName());
    }
}
