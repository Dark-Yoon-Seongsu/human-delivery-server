package goorm.humandelivery.call.controller;

import goorm.humandelivery.call.application.port.in.AcceptCallUseCase;
import goorm.humandelivery.call.dto.request.CallAcceptRequest;
import goorm.humandelivery.call.dto.response.CallAcceptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@MessageMapping("/taxi-driver")  // "/app/taxi-driver"
@RequiredArgsConstructor
public class WebSocketAcceptCallController {

    private final AcceptCallUseCase acceptCallUseCase;

    // 콜 요청 수락
    @MessageMapping("/accept-call")
    @SendToUser("/queue/accept-call-result")
    public CallAcceptResponse acceptTaxiCall(CallAcceptRequest request, Principal principal) {
        return acceptCallUseCase.acceptCall(request, principal.getName());
    }

}
