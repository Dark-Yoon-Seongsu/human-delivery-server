package goorm.humandelivery.application;

import java.time.Duration;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goorm.humandelivery.common.exception.IncorrectPasswordException;
import goorm.humandelivery.common.exception.TaxiDriverEntityNotFoundException;
import goorm.humandelivery.domain.model.entity.FuelType;
import goorm.humandelivery.domain.model.entity.Taxi;
import goorm.humandelivery.domain.model.entity.TaxiDriver;
import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.request.CreateTaxiDriverRequest;
import goorm.humandelivery.domain.model.request.CreateTaxiRequest;
import goorm.humandelivery.domain.model.request.LoginTaxiDriverRequest;
import goorm.humandelivery.domain.model.response.TaxiDriverResponse;
import goorm.humandelivery.domain.model.response.TaxiTypeResponse;
import goorm.humandelivery.domain.repository.TaxiDriverRepository;
import goorm.humandelivery.domain.repository.TaxiRepository;
import goorm.humandelivery.infrastructure.redis.key.RedisKeyParser;
import goorm.humandelivery.infrastructure.redis.RedisService;
import jakarta.persistence.EntityExistsException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TaxiDriverService {

	private final TaxiDriverRepository taxiDriverRepository;
	private final TaxiRepository taxiRepository;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final RedisService redisService;

	public TaxiDriverService(TaxiDriverRepository taxiDriverRepository, TaxiRepository taxiRepository,
		BCryptPasswordEncoder bCryptPasswordEncoder, RedisService redisService) {
		this.taxiDriverRepository = taxiDriverRepository;
		this.taxiRepository = taxiRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.redisService = redisService;
	}

	@Transactional
	public TaxiDriverResponse register(CreateTaxiDriverRequest request) {

		// 택시 만들기
		Taxi savedTaxi = createTaxi(request);

		// 택시기사 만들기
		TaxiDriver savedTaxiDriver = createTaxiDriver(request, savedTaxi);

		// 엔티티는 DTO 를 몰라야 한다.
		return TaxiDriverResponse.from(savedTaxiDriver);
	}

	public void validate(LoginTaxiDriverRequest loginTaxiDriverRequest) {

		TaxiDriver taxiDriver = taxiDriverRepository.findByLoginId(loginTaxiDriverRequest.getLoginId())
			.orElseThrow(TaxiDriverEntityNotFoundException::new);

		// 패스워드 검증
		boolean isSamePassword = taxiDriver.isSamePassword(loginTaxiDriverRequest.getPassword(), bCryptPasswordEncoder);

		if (!isSamePassword) {
			throw new IncorrectPasswordException();
		}
	}

	private Taxi createTaxi(CreateTaxiDriverRequest request) {
		CreateTaxiRequest createTaxiRequest = request.getTaxi();

		Taxi taxi = Taxi.builder()
			.fuelType(FuelType.valueOf(createTaxiRequest.getFuelType()))
			.taxiType(TaxiType.valueOf(createTaxiRequest.getTaxiType()))
			.plateNumber(createTaxiRequest.getPlateNumber())
			.model(createTaxiRequest.getModel())
			.build();

		return taxiRepository.save(taxi);
	}

	private TaxiDriver createTaxiDriver(CreateTaxiDriverRequest request, Taxi savedTaxi) {
		String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

		boolean isExist = taxiDriverRepository.existsByLoginId(request.getLoginId());

		if (isExist) {
			throw new EntityExistsException("이미 존재하는 택시기사 아이디입니다.");
		}

		TaxiDriver taxiDriver = TaxiDriver.builder()
			.taxi(savedTaxi)
			.loginId(request.getLoginId())
			.password(encodedPassword)
			.name(request.getName())
			.licenseCode(request.getLicenseCode())
			.phoneNumber(request.getPhoneNumber())
			.status(TaxiDriverStatus.OFF_DUTY)
			.build();

		// DB에 저장
		return taxiDriverRepository.save(taxiDriver);
	}

	@Transactional
	public TaxiDriverStatus changeStatus(String loginId, TaxiDriverStatus status) {

		log.info("[changeStatus.taxiDriverRepository.findByLoginId] 택시기사 조회. 택시기사 ID : {}", loginId);
		TaxiDriver taxiDriver = taxiDriverRepository.findByLoginId(loginId)
			.orElseThrow(TaxiDriverEntityNotFoundException::new);

		return taxiDriver.changeStatus(status);
	}

	public TaxiTypeResponse findTaxiDriverTaxiType(String loginId) {
		return taxiDriverRepository.findTaxiDriversTaxiTypeByLoginId(loginId)
			.orElseThrow(TaxiDriverEntityNotFoundException::new);
	}

	public TaxiDriverStatus getCurrentTaxiDriverStatus(String taxiDriverLoginId) {

		String key = RedisKeyParser.taxiDriverStatus(taxiDriverLoginId);

		// 1.Redis 조회
		String status = redisService.getValue(key);

		if (status != null) {
			return TaxiDriverStatus.valueOf(status);
		}

		// 2.없으면 DB 에서 조회.
		TaxiDriverStatus dbStatus = taxiDriverRepository.findByLoginId(taxiDriverLoginId)
			.orElseThrow(TaxiDriverEntityNotFoundException::new)
			.getStatus();

		// 3.이후 Redis 에 캐싱
		redisService.setValueWithTTL(key, dbStatus.name(), Duration.ofHours(1));

		return dbStatus;
	}

	public TaxiType getCurrentTaxiType(String taxiDriverLoginId) {
		String key = RedisKeyParser.taxiDriversTaxiType(taxiDriverLoginId);

		// 1. redis 조회
		String stringTaxiType = redisService.getValue(key);

		if (stringTaxiType != null) {
			return TaxiType.valueOf(stringTaxiType);
		}

		// 2. 없으면 DB 에서 조회
		TaxiTypeResponse taxiTypeResponse = taxiDriverRepository.findTaxiDriversTaxiTypeByLoginId(taxiDriverLoginId)
			.orElseThrow(TaxiDriverEntityNotFoundException::new);

		TaxiType taxiType = taxiTypeResponse.getTaxiType();

		// 3. 이후 redis 에 캐싱
		redisService.setValueWithTTL(key, taxiType.name(), Duration.ofDays(1));

		return taxiType;

	}

	public Long findIdByLoginId(String taxiDriverLoginId) {
		return taxiDriverRepository.findIdByLoginId(taxiDriverLoginId)
			.orElseThrow(TaxiDriverEntityNotFoundException::new);
	}
}
