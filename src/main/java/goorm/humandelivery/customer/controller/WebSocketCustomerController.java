package goorm.humandelivery.customer.controller;

import java.security.Principal;

import goorm.humandelivery.customer.service.CallMessageProcessingService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import goorm.humandelivery.domain.model.internal.CallMessage;
import goorm.humandelivery.domain.model.request.CallMessageRequest;
import goorm.humandelivery.domain.model.response.CallRequestMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 책임: 웹소켓 연결 및 클라이언트와의 상호작용을 담당하는 컨트롤러
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketCustomerController {

	private final CallMessageProcessingService webSocketCustomerService;
	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/call/request")
	@SendToUser("/queue/call/response")
	public CallRequestMessageResponse handleMessage(CallMessageRequest request, Principal principal) {
		log.info("서버에서 승객의 콜 수신");
		webSocketCustomerService.processMessage(request, principal.getName());

		return new CallRequestMessageResponse("콜이 성공적으로 요청되었습니다.");
	}

	public void cancelCall(){

	}

	public void sendCallMessageToTaxiDriver(String driverLoginId, CallMessage callMessage) {
		String destination = "/queue/call";
		messagingTemplate.convertAndSendToUser(
			driverLoginId,						// 사용자 이름(Principal name)
			destination, 						// 목적지
			callMessage);						// 전송할 메세지

	}

}
