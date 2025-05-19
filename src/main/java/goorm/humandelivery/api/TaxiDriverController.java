package goorm.humandelivery.api;

import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.request.NearbyDriversRequest;
import goorm.humandelivery.domain.model.response.TokenInfoResponse;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.infrastructure.redis.RedisService;
import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/taxi-driver")
public class TaxiDriverController {
    private final JwtTokenProviderPort jwtTokenProviderPort;
    private final RedisService redisService;

    public TaxiDriverController(JwtTokenProviderPort jwtTokenProviderPort, RedisService redisService) {
        this.jwtTokenProviderPort = jwtTokenProviderPort;
        this.redisService = redisService;
    }

    // 토큰 확인
    @GetMapping("/token-info")
    public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        TokenInfoResponse tokenInfoResponse = jwtTokenProviderPort.extractTokenInfo(token);
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
