package goorm.humandelivery.customer.controller;

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
    public void handleCallCancel(CancelCallMessage message, Principal principal) {
        log.info("[WebSocket] Call cancel request received: {}", message.getCallId());

        cancelCallUseCase.cancelCall(message.getCallId());

        CallCancelMessageResponse response = new CallCancelMessageResponse(
                "콜이 성공적으로 취소되었습니다.", message.getCallId()
        );

        // ✅ 메시지를 요청한 유저에게만 전송
        messagingTemplate.convertAndSendToUser(
                principal.getName(),           // 인증된 사용자명 (e.g. email)
                "/queue/call-cancelled",       // 클라이언트가 구독한 경로
                response
        );
    }
}
