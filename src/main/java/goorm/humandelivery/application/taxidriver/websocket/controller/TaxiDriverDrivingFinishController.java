package goorm.humandelivery.application.taxidriver.websocket.controller;

import goorm.humandelivery.domain.model.request.CallIdRequest;
import goorm.humandelivery.application.DrivingInfoService;
import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.response.DrivingSummaryResponse;
import goorm.humandelivery.application.taxidriver.TaxiDriverLoadService;
import goorm.humandelivery.infrastructure.messaging.MessagingService;
import goorm.humandelivery.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@Controller
@MessageMapping("/taxi-driver")  // "/app/taxi-driver"
public class TaxiDriverDrivingFinishController {

	private final RedisService redisService;
	private final TaxiDriverLoadService taxiDriverLoadService;
	private final MessagingService messagingService;
	private final DrivingInfoService drivingInfoService;


	/**
	 * 승객 하차 완료 요청 처리 /app/taxi-driver/ride-finish
	 */
	@MessageMapping("/ride-finish")
	public void finishDriving(CallIdRequest request, Principal principal) {

		/**
		 * 손님이 하차했다. 드라이빙 인포 조회해서 상태 바꾸고, 택시기사 상태 바꿔야한다.
		 * 그리고 손님과 택시기사에게 DrivingInfoResponse 를 전달해야 한다.
		 */
		log.info("[finishDriving.WebSocketTaxiDriverController] 하차 요청.  콜 ID : {}, 택시기사 ID : {}", request.getCallId(),
			principal.getName());

		/**
		 * TODO : DrivingInfo가 있는지 확인하고, 택시기사 확인하고, 고객 확인하고 -> 이후에 하차 요청 승인
		 */

		Long callId = request.getCallId();
		String taxiDriverLoginId = principal.getName();


		Location location = redisService.getDriverLocation(taxiDriverLoginId);

		DrivingSummaryResponse response = drivingInfoService.finishDriving(callId, location);

		// 택시기사 상태 바꾸기 -> 빈차

		log.info("[finishDriving.WebSocketTaxiDriverController] 택시기사 DB 상태 바꾸기 호출 전.  콜 ID : {}, 택시기사 ID : {}", request.getCallId(),
			principal.getName());
		TaxiDriverStatus changedStatus = taxiDriverLoadService.changeStatus(taxiDriverLoginId,
			TaxiDriverStatus.AVAILABLE);


		log.info("[finishDriving.WebSocketTaxiDriverController] 택시기사 REDIS 상태 바꾸기 호출 전.  콜 ID : {}, 택시기사 ID : {}", request.getCallId(),
			principal.getName());
		TaxiType taxiType = redisService.getDriversTaxiType(taxiDriverLoginId);
		redisService.handleTaxiDriverStatusInRedis(taxiDriverLoginId, changedStatus, taxiType);

		log.info("[finishDriving.WebSocketTaxiDriverController] 메세지 전송 전.  콜 ID : {}, 택시기사 ID : {}", request.getCallId(),
			principal.getName());
		messagingService.sendDrivingCompletedMessageToUser(response.getCustomerLoginId(), response);
		messagingService.sendDrivingCompletedMessageToTaxiDriver(taxiDriverLoginId, response);
	}
}
