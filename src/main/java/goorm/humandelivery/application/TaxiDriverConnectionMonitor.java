package goorm.humandelivery.application;

import static goorm.humandelivery.driver.domain.TaxiDriverStatus.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.infrastructure.messaging.MessagingService;
import goorm.humandelivery.infrastructure.redis.RedisService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TaxiDriverConnectionMonitor {

	private final RedisService redisService;
	private final MatchingService matchingService;
	private final TaxiDriverService taxiDriverService;
	private final MessagingService messagingService;
	private final CallInfoService callInfoService;

	private static final long TIMEOUT_MILLIS = 10_000;

	@Autowired
	public TaxiDriverConnectionMonitor(RedisService redisService, MatchingService matchingService,
		TaxiDriverService taxiDriverService, MessagingService messagingService, CallInfoService callInfoService) {
		this.redisService = redisService;
		this.matchingService = matchingService;
		this.taxiDriverService = taxiDriverService;
		this.messagingService = messagingService;
		this.callInfoService = callInfoService;
	}

	/**
	 * Scheduled 규칙
	 *    Method 는 void 타입으로
	 *    Method 는 매개변수 사용 불가
	 *    이전 작업 종류 이후 시점으로부터 정의된 시간만큼 지난 후 Task를 실행한다.
	 */

	// 5초에 한번씩 스케쥴링
	@Scheduled(fixedDelay = 5000)
	public void monitorReservedTaxiDrivers() {
		long now = System.currentTimeMillis();

		log.info("[monitorReservedTaxiDrivers.TaxiDriverConnectionMonitor] start monitoring at : {}",
			Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDateTime()
		);

		// 1. 운행중인 기사목록 조회
		Set<String> activeDrivers = redisService.getActiveDrivers();

		// 2. 기사들 중 RESERVED 상태인 기사만 조회
		List<String> reservedDrivers = activeDrivers.stream()
			.filter(driverId -> redisService.getDriverStatus(driverId) == RESERVED)
			.toList();

		// 3. reservedDrivers 의 마지막 위치정보 시간 조회
		for (String driverLoginId : reservedDrivers) {
			String lastUpdateStr = redisService.getLastUpdate(driverLoginId);

			if (lastUpdateStr == null || now - Long.parseLong(lastUpdateStr) > TIMEOUT_MILLIS) {
				log.warn("[{}] 위치 갱신 시간 초과.", driverLoginId);

				// 1. call Id 조회
				Optional<String> callIdOptional = redisService.getCallIdByDriverId(driverLoginId);

				if (callIdOptional.isEmpty()) {
					// 해당 택시기사 상태 OFF_DUTY 로 DB 에서 변경..
					TaxiType taxiType = redisService.getDriversTaxiType(driverLoginId);

					// 택시기사 상태 변경에 따른 Redis 내부 처리 로직 수행
					TaxiDriverStatus taxiDriverStatus = taxiDriverService.changeStatus(driverLoginId, OFF_DUTY);
					redisService.handleTaxiDriverStatusInRedis(driverLoginId, taxiDriverStatus, taxiType);

					log.info(
						"[monitorReservedTaxiDrivers.TaxiDriverConnectionMonitor] 배차 실패. 택시기사 ID : {}", driverLoginId);
					continue;
				}


				Long callId = Long.valueOf(callIdOptional.get());
				String customerLoginId = callInfoService.findCustomerLoginIdById(callId);

				// 매칭 엔티티 삭제
				matchingService.deleteByCallId(callId);

				// 3. 해당 택시기사 상태 OFF_DUTY 로 DB 에서 변경..
				TaxiType taxiType = redisService.getDriversTaxiType(driverLoginId);

				// 4. 택시기사 상태 변경에 따른 Redis 내부 처리 로직 수행
				TaxiDriverStatus taxiDriverStatus = taxiDriverService.changeStatus(driverLoginId, OFF_DUTY);
				redisService.handleTaxiDriverStatusInRedis(driverLoginId, taxiDriverStatus, taxiType);

				// 5. 고객 및 택시에게 예외 메세지 전송
				log.info(
					"[monitorReservedTaxiDrivers.TaxiDriverConnectionMonitor] 배차 실패. 예외 메세지 전송. 콜 ID : {}, 유저 ID : {}, 택시기사 ID : {}",
					callId, customerLoginId, driverLoginId);
				messagingService.sendDispatchFailMessageToUser(customerLoginId);
				messagingService.sendDispatchFailMessageToTaxiDriver(driverLoginId);

			}

		}

	}

}
