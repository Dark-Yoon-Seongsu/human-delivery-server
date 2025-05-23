package goorm.humandelivery.application.taxidriver.websocket.controller;

import goorm.humandelivery.domain.model.request.*;
import goorm.humandelivery.domain.model.response.CallAcceptResponse;
import goorm.humandelivery.domain.model.response.CallRejectResponse;
import goorm.humandelivery.application.taxidriver.websocket.service.TaxiDriverCallResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
@MessageMapping("/taxi-driver")
public class TaxiDriverCallResponseController {

	private final TaxiDriverCallResponseService taxiDriverCallResponseService;

	@MessageMapping("/accept-call")
	@SendToUser("/queue/accept-call-result")
	public CallAcceptResponse acceptTaxiCall(CallAcceptRequest request, Principal principal) {

		return taxiDriverCallResponseService.acceptTaxiCall(request, principal.getName());
	}

	@MessageMapping("/reject-call")
	@SendToUser("/queue/reject-call-result")
	public CallRejectResponse rejectTaxiCall(CallRejectRequest request, Principal principal) {
		return taxiDriverCallResponseService.rejectTaxiCall(request, principal.getName());
	}
}

