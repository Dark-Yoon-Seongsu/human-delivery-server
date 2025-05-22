package goorm.humandelivery.service;

import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.internal.CallMessage;
import goorm.humandelivery.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NearTaxiSearchService {

    private final RedisService redisService;

    /**
     * 특정 위치 기준 반경 내 사용 가능한 택시 드라이버 ID 리스트 조회
     *
     * @param callId        호출 ID (현재 테스트 코드상 미사용)
     * @param requestedType 요청한 택시 타입
     * @param centerLat     중심 위도
     * @param centerLon     중심 경도
     * @param radiusKm      반경 (km)
     * @return 조건에 맞는 드라이버 ID 리스트
     */
    public List<String> findNearByAvailableDrivers(CallMessage callMassage) {
        List<String> nearbyDrivers = new ArrayList<>();

        int radiusInKm = 5 * callMassage.getRetryCount();

        if (radiusInKm <= 0) {
            return nearbyDrivers;
        }

        Set<String> activeDrivers = redisService.getActiveDrivers();
        if (activeDrivers == null || activeDrivers.isEmpty()) {
            return nearbyDrivers;
        }

        TaxiType requestedType = callMassage.getTaxiType();
        Location origin = callMassage.getExpectedOrigin();

        for (String driverId : activeDrivers) {
            if (!isDriverMatch(driverId, requestedType, origin, radiusInKm)) {
                continue;
            }
            nearbyDrivers.add(driverId);
        }

        return nearbyDrivers;
    }

    /**
     * 드라이버가 요청 조건에 맞는지 검사
     */
    private boolean isDriverMatch(String driverId, TaxiType requestedType, Location center, double radiusKm) {
        TaxiType driverType = redisService.getDriversTaxiType(driverId);
        if (driverType == null || !driverType.equals(requestedType)) {
            return false;
        }

        Location driverLocation = redisService.getDriverLocation(driverId);
        if (driverLocation == null) {
            return false;
        }

        double distanceKm = calculateDistanceKm(
                center.getLatitude(), center.getLongitude(),
                driverLocation.getLatitude(), driverLocation.getLongitude()
        );

        return distanceKm <= radiusKm;
    }

    /**
     * 두 위치(위도, 경도) 간의 거리 계산 (Haversine 공식)
     *
     * @param lat1 위도1
     * @param lon1 경도1
     * @param lat2 위도2
     * @param lon2 경도2
     * @return 두 점 사이 거리 (km)
     */
    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
