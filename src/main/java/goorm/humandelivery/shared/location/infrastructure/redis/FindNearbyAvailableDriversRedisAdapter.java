package goorm.humandelivery.shared.location.infrastructure.redis;

import goorm.humandelivery.call.application.port.out.CheckDriverRejectedForCallRedisPort;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.infrastructure.redis.RedisKeyParser;
import goorm.humandelivery.shared.location.application.port.out.FindNearbyAvailableDriversRedisPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class FindNearbyAvailableDriversRedisAdapter implements FindNearbyAvailableDriversRedisPort {

    private final StringRedisTemplate redisTemplate;
    private final CheckDriverRejectedForCallRedisPort checkDriverRejectedForCallRedisPort;

    @Override
    public List<String> findNearByAvailableDrivers(Long callId, TaxiType taxiType, double latitude, double longitude, double radiusInKm) {
        String key = RedisKeyParser.getTaxiDriverLocationKeyBy(TaxiDriverStatus.AVAILABLE, taxiType);

        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
            geoOps.radius(key, new Circle(new Point(longitude, latitude), new Distance(radiusInKm, Metrics.KILOMETERS)));

        if (results == null) return List.of();

        return results.getContent().stream()
            .map(GeoResult::getContent)
            .map(GeoLocation::getName)
            .filter(id -> !checkDriverRejectedForCallRedisPort.isDriverRejected(callId, id))
            .toList();
    }
}