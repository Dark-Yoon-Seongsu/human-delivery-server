package goorm.humandelivery.application.taxidriver.websocket.controller;

import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.request.*;
import goorm.humandelivery.application.taxidriver.TaxiDriverLoadService;
import goorm.humandelivery.infrastructure.redis.RedisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
@MessageMapping("/taxi-driver")
public class TaxiDriverStatusController {

	private final RedisService redisService;
	private final TaxiDriverLoadService taxiDriverLoadService;

	@MessageMapping("/update-status")
	@SendToUser("/queue/taxi-driver-status")
	public UpdateTaxiDriverStatusResponse updateStatus(@Valid @RequestBody UpdateTaxiDriverStatusRequest request, Principal principal) {
		String loginId = principal.getName();
		TaxiDriverStatus newStatus = TaxiDriverStatus.valueOf(request.getStatus());

		TaxiDriverStatus changedStatus = taxiDriverLoadService.changeStatus(loginId, newStatus);
		TaxiType taxiType = taxiDriverLoadService.findTaxiDriverTaxiType(loginId).getTaxiType();

		return redisService.handleTaxiDriverStatusInRedis(loginId, changedStatus, taxiType);
	}
}
