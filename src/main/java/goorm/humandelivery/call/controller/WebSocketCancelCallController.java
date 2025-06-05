package goorm.humandelivery.call.controller;

import goorm.humandelivery.call.application.port.in.CancelCallUseCase;
import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.call.dto.request.CancelCallMessage;
import goorm.humandelivery.customer.dto.response.CallCancelMessageResponse;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.shared.messaging.CallMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@MessageMapping("/call")
public class WebSocketCancelCallController {

    private final CancelCallUseCase cancelCallUseCase;
    private final SimpMessagingTemplate messagingTemplate;
    // 승객이 호출 취소 요청을 보냄
    @MessageMapping("/cancel") // /app/call/cancel
    @SendToUser("/queue/call-cancelled") // /user/{username}/queue/call-cancelled
    public CallCancelMessageResponse handleCallCancel(CancelCallMessage message, Principal principal) {
        log.info("[WebSocket] Call cancel request received: {} loginId : {}", message.getCallId(), principal.getName());

        cancelCallUseCase.cancelCall(message.getCallId());

        return new CallCancelMessageResponse(
                "콜이 성공적으로 취소되었습니다.", message.getCallId()
        );

    }
}
