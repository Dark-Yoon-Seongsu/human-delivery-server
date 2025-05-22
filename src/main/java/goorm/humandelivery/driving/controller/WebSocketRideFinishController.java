package goorm.humandelivery.driving.controller;

import goorm.humandelivery.call.dto.request.CallIdRequest;
import goorm.humandelivery.driving.application.port.in.RideFinishUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@MessageMapping("/taxi-driver")  // "/app/taxi-driver"
@RequiredArgsConstructor
public class WebSocketRideFinishController {

    private final RideFinishUseCase rideFinishUseCase;

    @MessageMapping("/ride-finish")
    public void finishRide(@Valid CallIdRequest request, Principal principal) {
        rideFinishUseCase.finish(request.getCallId(), principal.getName());
    }
}
