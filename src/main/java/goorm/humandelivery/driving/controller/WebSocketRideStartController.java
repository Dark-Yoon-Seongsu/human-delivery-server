package goorm.humandelivery.driving.controller;

import goorm.humandelivery.call.dto.request.CallIdRequest;
import goorm.humandelivery.driving.application.port.in.RideStartUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@MessageMapping("/taxi-driver")
@RequiredArgsConstructor
public class WebSocketRideStartController {

    private final RideStartUseCase rideStartUseCase;

    @MessageMapping("/ride-start")
    public void createDrivingInfo(CallIdRequest request, Principal principal) {
        rideStartUseCase.rideStart(request.getCallId(), principal.getName());
    }
}