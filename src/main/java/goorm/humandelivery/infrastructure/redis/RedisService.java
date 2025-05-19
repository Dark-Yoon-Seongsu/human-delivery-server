package goorm.humandelivery.infrastructure.redis;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.stereotype.Service;

import goorm.humandelivery.common.exception.LocationNotInRedisException;
import goorm.humandelivery.common.exception.RedisKeyNotFoundException;
import goorm.humandelivery.domain.model.entity.CallStatus;
import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.domain.model.request.UpdateTaxiDriverStatusResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisService {

	private final StringRedisTemplate redisTemplate;

	@Autowired
	public RedisService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public String getValue(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public String getLastUpdate(String reservedDriver) {
		String key = RedisKeyParser.taxiDriverLastUpdate(reservedDriver);
		return redisTemplate.opsForValue().get(key);
	}

	public void setValueWithTTL(String key, String value, Duration ttl) {
		redisTemplate.opsForValue().set(key, value, ttl);
	}

	public void setValueWithTTLIfAbsent(String key, String value, Duration ttl) {
		redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
	}

	public void setLocation(String key, String loginId, Location location) {
		redisTemplate.opsForGeo().add(key, new Point(location.getLongitude(), location.getLatitude()), loginId);
	}

	public Location getLocation(String key, String loginId) {
		List<Point> position = redisTemplate.opsForGeo()
			.position(key, loginId);

		if (position == null) {
			throw new LocationNotInRedisException(key, loginId);
		}

		Point point = position
			.stream()
			.findFirst()
			.orElseThrow(() -> new LocationNotInRedisException(key, loginId));

		return new Location(point.getY(), point.getX()); // 위도, 경도 순서
	}

	public void setCallWith(Long callId, CallStatus callStatus) {
		String key = RedisKeyParser.callStatus(callId);
		redisTemplate.opsForValue().set(key, callStatus.name(), Duration.ofHours(1));
	}

	public CallStatus getCallStatus(Long callId) {
		String statusStr = redisTemplate.opsForValue().get(RedisKeyParser.callStatus(callId));
		return statusStr == null ? CallStatus.DONE : CallStatus.valueOf(statusStr);
	}

	public void setDriversStatus(String taxiDriverLoginId, TaxiDriverStatus status) {
		String statusKey = RedisKeyParser.taxiDriverStatus(taxiDriverLoginId);
		setValueWithTTL(statusKey, status.name(), Duration.ofHours(1));
	}

	public TaxiDriverStatus getDriverStatus(String driverId) {
		String key = RedisKeyParser.taxiDriverStatus(driverId);
		String statusStr = redisTemplate.opsForValue().get(key);

		if (statusStr == null) {
			return TaxiDriverStatus.OFF_DUTY;
		}
		return TaxiDriverStatus.valueOf(statusStr);
	}

	public void setActive(String taxiDriverLoginId) {
		redisTemplate.opsForSet().add(RedisKeyParser.ACTIVE_TAXI_DRIVER_KEY, taxiDriverLoginId);
	}

	public Set<String> getActiveDrivers() {
		Set<String> members = redisTemplate.opsForSet().members(RedisKeyParser.ACTIVE_TAXI_DRIVER_KEY);
		return members == null ? Set.of() : members;
	}

	public void setOffDuty(String taxiDriverLoginId) {
		redisTemplate.opsForSet().remove(RedisKeyParser.ACTIVE_TAXI_DRIVER_KEY, taxiDriverLoginId);
	}

	public void setDriversTaxiType(String taxiDriverLoginId, TaxiType taxiType) {
		String taxiTypeKey = RedisKeyParser.taxiDriversTaxiType(taxiDriverLoginId);
		setValueWithTTLIfAbsent(taxiTypeKey, taxiType.name(), Duration.ofDays(1));
	}

	public TaxiType getDriversTaxiType(String taxiDriverLoginId) {
		String taxiTypeKey = RedisKeyParser.taxiDriversTaxiType(taxiDriverLoginId);

		TaxiType taxiType = TaxiType.valueOf(redisTemplate.opsForValue().get(taxiTypeKey));

		if (taxiType == null) {
			throw new RedisKeyNotFoundException(taxiTypeKey);
		}

		return taxiType;
	}

	public boolean tryAcceptCall(String callId, String taxiDriverLoginId) {
		// Nullable Boolean 반환. 아래와 같이 NullPointerException 처리.
		return Boolean.TRUE.equals(
			redisTemplate.opsForValue().setIfAbsent(callId, taxiDriverLoginId, Duration.ofMinutes(5))
		);
	}

	public void removeFromLocation(String taxiDriverLoginId, TaxiType taxiType, TaxiDriverStatus driverStatus) {
		String key = RedisKeyParser.getTaxiDriverLocationKeyBy(driverStatus, taxiType);
		redisTemplate.opsForZSet().remove(key, taxiDriverLoginId);
	}

	public void addRejectedDriverToCall(Long callId, String taxiDriverLoginId) {
		String key = RedisKeyParser.getRejectCallKey(callId);
		// 운행 완료 후에 해당 set 딜리트 필요.
		redisTemplate.opsForSet().add(key, taxiDriverLoginId);
	}

	public boolean isDriverRejectedForCall(Long callId, String taxiDriverLoginId) {
		String key = RedisKeyParser.getRejectCallKey(callId);
		return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, taxiDriverLoginId));
	}

	public List<String> findNearByAvailableDrivers(Long callId, TaxiType taxiType, double latitude, double longitude,
		double radiusInKm) {

		String key = RedisKeyParser.getTaxiDriverLocationKeyBy(TaxiDriverStatus.AVAILABLE, taxiType);

		GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

		// new Point 내부 인자는 경도 / 위도 순서로 넣습니다
		GeoResults<RedisGeoCommands.GeoLocation<String>> results =
			geoOps.radius(key, new Circle(new Point(longitude, latitude), new Distance(radiusInKm, Metrics.KILOMETERS))
			);

		if (results == null) {
			return List.of();
		}

		List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = results.getContent();

		List<String> driversIds = content.stream()
			.map(GeoResult::getContent)
			.map(GeoLocation::getName)
			.toList();

		/**
		 * 여기서 이미 해당 콜을 거절한 기사인지 체크하고, 거절하지 않은 기사들에게만 콜 요청을 보냅니다.
		 */
		return driversIds.stream().filter(id -> !isDriverRejectedForCall(callId, id)).toList();
	}

	public void assignCallToDriver(Long callId, String taxiDriverLoginId) {
		String key = RedisKeyParser.assignCallToDriver(taxiDriverLoginId);

		// taxidriver:기사아이디:call, callId
		redisTemplate.opsForValue().set(key, String.valueOf(callId));
	}

	public Optional<String> getCallIdByDriverId(String taxiDriverLoginId) {
		String key = RedisKeyParser.assignCallToDriver(taxiDriverLoginId);
		return Optional.ofNullable(redisTemplate.opsForValue().get(key));
	}

	public void deleteCallStatus(Long callId) {
		String key = RedisKeyParser.callStatus(callId);
		redisTemplate.delete(key);
	}

	public void deleteAssignedCallOf(String driverLoginId) {
		String key = RedisKeyParser.assignCallToDriver(driverLoginId);
		redisTemplate.delete(key);
	}

	public void removeRejectedDriversForCall(Long callId) {
		String key = RedisKeyParser.getRejectCallKey(callId);
		redisTemplate.delete(key);
	}

	public UpdateTaxiDriverStatusResponse handleTaxiDriverStatusInRedis(String taxiDriverLoginId,
		TaxiDriverStatus changedStatus, TaxiType taxiType) {

		// [공통]
		// Redis 에 택시기사 상태 저장. TTL : 1시간
		log.info("[updateStatus : redis 택시기사 상태 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId, changedStatus);
		setDriversStatus(taxiDriverLoginId, changedStatus);

		// Redis 에 택시기사의 택시 종류 저장. TTL : 1일
		log.info("[updateStatus : redis 택시기사 종류 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId, changedStatus);
		setDriversTaxiType(taxiDriverLoginId, taxiType);

		// [상태별 처리]
		if (changedStatus == TaxiDriverStatus.OFF_DUTY) {
			// 운행 종료. active 택시기사 목록에서 제외
			log.info("[updateStatus : 택시기사 비활성화. active 목록에서 제외] taxiDriverId : {}, 상태 : {}", taxiDriverLoginId,
				changedStatus);
			setOffDuty(taxiDriverLoginId);

			// 해당 기사의 위치정보 삭제
			deleteAllLocationDataInRedis(taxiDriverLoginId, taxiType);

			// 해당 기사가 가지고 있던 콜 삭제
			deleteCallBy(taxiDriverLoginId);

		}

		if (changedStatus == TaxiDriverStatus.AVAILABLE) {

			deleteCallBy(taxiDriverLoginId);

			// 위치정보도 삭제
			removeFromLocation(taxiDriverLoginId, taxiType, TaxiDriverStatus.RESERVED);
			removeFromLocation(taxiDriverLoginId, taxiType, TaxiDriverStatus.ON_DRIVING);

			log.info("[updateStatus : redis 택시기사 active set 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId,
				changedStatus);

			// active driver set 에 없으면 추가
			setActive(taxiDriverLoginId);
		}

		if (changedStatus == TaxiDriverStatus.RESERVED) {

			// 위치정보 삭제
			removeFromLocation(taxiDriverLoginId, taxiType, TaxiDriverStatus.AVAILABLE);
			removeFromLocation(taxiDriverLoginId, taxiType, TaxiDriverStatus.ON_DRIVING);

			// redis 에 저장된 콜 상태 변경  SENT -> DONE
		   Optional<String> callIdOptional = getCallIdByDriverId(taxiDriverLoginId);

			/*if (callIdOptional.isEmpty()) {
				throw new RedisKeyNotFoundException("현재 기사가 가진 콜 정보가 Redis 에 존재하지 않습니다.");
			}*/

			 Long callId = Long.valueOf(callIdOptional.get());

			// 해당 택시기사가 담당받은 콜 정보를 redis 에 저장.
			//setCallWith(callId, CallStatus.DONE);

			// 콜에 대한 거부 택시 기사목록 삭제
			removeRejectedDriversForCall(callId);

			log.info("[updateStatus : redis 택시기사 active set 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId,
				changedStatus);

			// active driver set 에 없으면 추가
			setActive(taxiDriverLoginId);
		}

		if (changedStatus == TaxiDriverStatus.ON_DRIVING) {
			Optional<String> callIdOptional = getCallIdByDriverId(taxiDriverLoginId);

			if (callIdOptional.isEmpty()) {
				throw new RedisKeyNotFoundException("현재 기사가 가진 콜 정보가 Redis 에 존재하지 않습니다.");
			}

			Long callId = Long.valueOf(callIdOptional.get());

			// 위치정보 삭제
			removeFromLocation(taxiDriverLoginId, taxiType, TaxiDriverStatus.AVAILABLE);
			removeFromLocation(taxiDriverLoginId, taxiType, TaxiDriverStatus.RESERVED);

			// 콜에 대한 거부 택시 기사목록 삭제
			removeRejectedDriversForCall(callId);

			log.info("[updateStatus : redis 택시기사 active set 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId,
				changedStatus);

			// active driver set 에 없으면 추가
			setActive(taxiDriverLoginId);
		}

		return new UpdateTaxiDriverStatusResponse(changedStatus);

	}

	private void deleteAllLocationDataInRedis(String taxiDriverLoginId, TaxiType taxiType) {
		removeFromLocation(taxiDriverLoginId, taxiType, TaxiDriverStatus.AVAILABLE);
		removeFromLocation(taxiDriverLoginId, taxiType, TaxiDriverStatus.RESERVED);
		removeFromLocation(taxiDriverLoginId, taxiType, TaxiDriverStatus.ON_DRIVING);
	}

	public void deleteCallBy(String taxiDriverLoginId) {
		log.info("[deleteCallBy.RedisService 호출] taxiDriverId : {}", taxiDriverLoginId);

		Optional<String> callIdStr = getCallIdByDriverId(taxiDriverLoginId);

		if (callIdStr.isPresent()) {
			Long callId = Long.valueOf(callIdStr.get());
			deleteCallStatus(callId); // 콜 상태 삭제
			deleteAssignedCallOf(taxiDriverLoginId); // 해당 기사가 담당했던 콜 삭제.
			redisTemplate.delete(String.valueOf(callId)); // 콜 자체를 삭제
			removeRejectedDriversForCall(callId); // 해당 콜에 대한 거절 택시기사 목록 삭제
			return;
		}

		log.info("[deleteCallBy.RedisService 호출] 해당 기사가 가진 콜 정보가 없습니다. taxiDriverId : {}", taxiDriverLoginId);

	}

	public Location getDriverLocation(String driverLoginId) {

		TaxiDriverStatus taxiDriverStatus = getDriverStatus(driverLoginId);
		TaxiType taxiType = getDriversTaxiType(driverLoginId);
		String key = RedisKeyParser.getTaxiDriverLocationKeyBy(taxiDriverStatus, taxiType);

		return getLocation(key, driverLoginId);

	}

	public boolean hasAssignedCall(String taxiDriverLoginId) {
		String key = RedisKeyParser.assignCallToDriver(taxiDriverLoginId);
		return redisTemplate.hasKey(key);
	}

	public boolean atomicAcceptCall(Long callId, String driverLoginId) {
		String callStatusKey = RedisKeyParser.callStatus(callId); // e.g. call:123:status
		String driverCallKey = RedisKeyParser.assignCallToDriver(
			driverLoginId);  // e.g. taxidriver:driver001@example.com:call
		String driverStatusKey = RedisKeyParser.taxiDriverStatus(
			driverLoginId); // e.g. taxidriver:driver001@example.com:status

		String lockKey = String.valueOf(callId);

		String lua = """
			      local callStatus = redis.call('GET', KEYS[1])
			      local driverCall = redis.call('EXISTS', KEYS[2])
			      local driverStatus = redis.call('GET', KEYS[3])
			
			      if callStatus ~= ARGV[1] then
			          return 1 -- 콜 상태가 SENT 아님
			      end
			
			      if driverCall == 1 then
			          return 2 -- 이미 기사에게 콜 할당됨
			      end
			
			      if driverStatus ~= ARGV[2] then
			          return 3 -- 기사 상태가 AVAILABLE 아님
			      end
			
			      local success = redis.call('SETNX', KEYS[4], ARGV[3])
			      if success == 0 then
			          return 4 -- SETNX 실패 (다른 사람이 먼저 수락)
			      end
			
			      -- 여기서 상태 변화까지 시켜야 한다.
			    redis.call('SET', KEYS[1], ARGV[4]) -- callStatus = DONE
				redis.call('SET', KEYS[3], ARGV[5]) -- driverStatus = RESERVED
				redis.call('SET', KEYS[2], ARGV[6]) -- driverCall = callId
			      return 0;
			""";

		DefaultRedisScript<Long> script = new DefaultRedisScript<>();
		script.setScriptText(lua);
		script.setResultType(Long.class);

		List<String> keys = List.of(
			callStatusKey, driverCallKey, driverStatusKey, lockKey
		);
		List<String> args = List.of("SENT", "AVAILABLE", driverLoginId, "DONE", "RESERVED", String.valueOf(callId));

		Long result = redisTemplate.execute(script, keys, args.toArray());
		return result != null && result == 0L;
	}
}
