package goorm.humandelivery.call.controller;

import goorm.humandelivery.call.application.port.in.RejectCallUseCase;
import goorm.humandelivery.call.dto.request.CallRejectRequest;
import goorm.humandelivery.call.dto.response.CallRejectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@MessageMapping("/taxi-driver")  // "/app/taxi-driver"
@RequiredArgsConstructor
public class WebSocketRejectCallController {

    private final RejectCallUseCase rejectCallUseCase;

    // 콜 요청 거절
    @MessageMapping("/reject-call")
    @SendToUser("/queue/reject-call-result")
    public CallRejectResponse rejectTaxiCall(CallRejectRequest request, Principal principal) {
        return rejectCallUseCase.addRejectedDriverToCall(request.getCallId(), principal.getName());
    }
}
