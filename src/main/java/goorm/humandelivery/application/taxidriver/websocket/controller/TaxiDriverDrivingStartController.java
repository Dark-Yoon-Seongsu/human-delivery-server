package goorm.humandelivery.application.taxidriver.websocket.controller;

import goorm.humandelivery.domain.model.request.CallIdRequest;
import goorm.humandelivery.application.CallInfoService;
import goorm.humandelivery.application.DrivingInfoService;
import goorm.humandelivery.application.MatchingService;
import goorm.humandelivery.domain.model.entity.DrivingInfo;
import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.request.*;
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
public class TaxiDriverDrivingStartController {

	private final RedisService redisService;
	private final TaxiDriverLoadService taxiDriverLoadService;
	private final MessagingService messagingService;
	private final MatchingService matchingService;
	private final CallInfoService callInfoService;
	private final DrivingInfoService drivingInfoService;

	/**
	 * 승객 승차 완료 요청 처리
	 */
	@MessageMapping("/ride-start")
	public void createDrivingInfo(CallIdRequest request, Principal principal) {

		log.info("[createDrivingInfo.WebSocketTaxiDriverController] 고객 승차.  콜 ID : {}, 택시기사 ID : {}", request.getCallId(),
			principal.getName());

		/**
		 * TODO : 검증 필요. Matching Id와 callId가 같은지 확인해야함
		 */

		Long callId = request.getCallId();
		String taxiDriverLoginId = principal.getName();


		// 해당 택시기사가

		/**
		 * 손님 타고, 택시기사가 손님 탑승 확인 요청을 보냄. 이후 운행정보 엔티티 생성.
		 */
		Long matchingId = matchingService.findMatchingIdByCallId(request.getCallId());
		Location taxiDriverLocation = redisService.getDriverLocation(principal.getName());

		CreateDrivingInfoRequest drivingInfoRequest = new CreateDrivingInfoRequest(matchingId,
			taxiDriverLocation);

		// 운행정보 엔티티 생성
		DrivingInfo savedDrivingInfo = drivingInfoService.create(drivingInfoRequest);

		// 택시 상태 변경
		TaxiDriverStatus changedStatus = taxiDriverLoadService.changeStatus(taxiDriverLoginId,
			TaxiDriverStatus.ON_DRIVING);

		// 레디스 상태 변경
		TaxiType taxiType = redisService.getDriversTaxiType(taxiDriverLoginId);
		redisService.handleTaxiDriverStatusInRedis(taxiDriverLoginId, changedStatus, taxiType);

		// 운행 시작 메세지 전달
		String customerLoginId = callInfoService.findCustomerLoginIdById(callId);
		boolean isDrivingStarted = savedDrivingInfo.isDrivingStarted();

		// 응답 반환.
		messagingService.sendDrivingStartMessageToUser(customerLoginId, isDrivingStarted, false);
		messagingService.sendDrivingStartMessageToTaxiDriver(taxiDriverLoginId, isDrivingStarted, false);
	}


}
