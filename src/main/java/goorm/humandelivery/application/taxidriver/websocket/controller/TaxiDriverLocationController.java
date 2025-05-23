package goorm.humandelivery.application.taxidriver.websocket.controller;

import goorm.humandelivery.common.exception.OffDutyLocationUpdateException;
import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.request.*;
import goorm.humandelivery.application.taxidriver.TaxiDriverLoadService;
import goorm.humandelivery.infrastructure.messaging.MessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@Controller
@MessageMapping("/taxi-driver")
public class TaxiDriverLocationController {

	private final TaxiDriverLoadService taxiDriverLoadService;
	private final MessagingService messagingService;

	@MessageMapping("/update-location")
	public void updateLocation(UpdateLocationRequest request, Principal principal) {
		String loginId = principal.getName();
		TaxiDriverStatus status = taxiDriverLoadService.getCurrentTaxiDriverStatus(loginId);

		if (status == TaxiDriverStatus.OFF_DUTY) throw new OffDutyLocationUpdateException();

		TaxiType taxiType = taxiDriverLoadService.getCurrentTaxiType(loginId);
		messagingService.sendLocation(loginId, status, taxiType, request.getCustomerLoginId(), request.getLocation());
	}
}
