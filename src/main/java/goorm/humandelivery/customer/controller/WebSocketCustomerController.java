package goorm.humandelivery.customer.controller;

import goorm.humandelivery.call.application.port.in.RequestCallUseCase;
import goorm.humandelivery.call.dto.request.CallMessageRequest;
import goorm.humandelivery.call.dto.response.CallRequestMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

// 책임: 웹소켓 연결 및 클라이언트와의 상호작용을 담당하는 컨트롤러
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketCustomerController {

    private final RequestCallUseCase requestCallUseCase;

    @MessageMapping("/call/request")
    @SendToUser("/queue/call/response")
    public CallRequestMessageResponse handleMessage(CallMessageRequest request, Principal principal) {
        log.info("서버에서 승객의 콜 수신");
        requestCallUseCase.requestCall(request, principal.getName());
        return new CallRequestMessageResponse("콜이 성공적으로 요청되었습니다.");
    }
}
