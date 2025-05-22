package goorm.humandelivery.driver;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goorm.humandelivery.common.security.jwt.JwtUtil;
import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.request.CreateTaxiDriverRequest;
import goorm.humandelivery.domain.model.request.LoginTaxiDriverRequest;
import goorm.humandelivery.domain.model.request.NearbyDriversRequest;
import goorm.humandelivery.domain.model.response.JwtResponse;
import goorm.humandelivery.domain.model.response.TaxiDriverResponse;
import goorm.humandelivery.domain.model.response.TokenInfoResponse;
import goorm.humandelivery.infrastructure.redis.RedisService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/taxi-driver")
public class TaxiDriverController {

	private final TaxiDriverService taxiDriverService;
	private final JwtUtil jwtUtil;
	private final RedisService redisService;

	public TaxiDriverController(JwtUtil jwtUtil, RedisService redisService, TaxiDriverService taxiDriverService) {
		this.jwtUtil = jwtUtil;
		this.redisService = redisService;
		this.taxiDriverService = taxiDriverService;
	}

	// 회원가입
	@PostMapping
	public ResponseEntity<?> register(@RequestBody @Valid CreateTaxiDriverRequest taxiDriverRequest,
		BindingResult bindingResult) {

		// 밸리데이션 응답 추가.
		if (bindingResult.hasErrors()) {
			List<String> fieldErrors = bindingResult.getFieldErrors()
				.stream()
				.map(FieldError::getDefaultMessage)
				.toList();

			return ResponseEntity.badRequest().body(Map.of("errors", fieldErrors));
		}

		TaxiDriverResponse response = taxiDriverService.register(taxiDriverRequest);
		return ResponseEntity.ok(response);
	}

	// 로그인
	@PostMapping("/auth-tokens")
	public ResponseEntity<?> loginTaxiDriver(@RequestBody @Valid LoginTaxiDriverRequest loginTaxiDriverRequest) {
		// 아이디 찾고.. 패스워드 암호화 후 일치하는 지 확인해야 함.. 서비스에서 해야겠네

		taxiDriverService.validate(loginTaxiDriverRequest);

		// 있으면? -> 토큰 만들어서 돌려줌
		String token = jwtUtil.generateToken(loginTaxiDriverRequest.getLoginId());

		return ResponseEntity.ok(new JwtResponse(token));
	}

	// 토큰 확인
	@GetMapping("/token-info")
	public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		TokenInfoResponse tokenInfoResponse = jwtUtil.extractTokenInfo(token);
		return ResponseEntity.ok(tokenInfoResponse);
	}

	// 인근 드라이버 확인 메서드 - 테스트 용도입니다.
	@PostMapping("/search/nearbydrivers")
	public ResponseEntity<?> findNearByDrivers(@Valid @RequestBody NearbyDriversRequest request) {
		log.info("인근 드라이버 확인 메서드");

		// 종류별로 해야한다.
		Location location = request.getLocation();
		Double latitude = location.getLatitude();
		Double longitude = location.getLongitude();
		Double radiusInKm = request.getRadiusInKm();
		TaxiType taxiType = request.getTaxiType();

		List<String> nearByDrivers = redisService.findNearByAvailableDrivers(1L, taxiType, latitude,
			longitude, radiusInKm);

		if (nearByDrivers.isEmpty()) {
			radiusInKm += 5;
			nearByDrivers = redisService.findNearByAvailableDrivers(1L, taxiType, latitude, longitude, radiusInKm);
			log.info("반경 확장 후 재조회: {}km", radiusInKm);
		}

		log.info("nearByDrivers : {}", nearByDrivers);
		return ResponseEntity.ok(nearByDrivers);
	}
}
