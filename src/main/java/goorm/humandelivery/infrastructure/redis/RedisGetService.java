package goorm.humandelivery.infrastructure.redis;

import goorm.humandelivery.common.exception.LocationNotInRedisException;
import goorm.humandelivery.common.exception.RedisKeyNotFoundException;
import goorm.humandelivery.domain.model.entity.CallStatus;
import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import goorm.humandelivery.domain.model.entity.Location;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisGetService {

    private final StringRedisTemplate redisTemplate;

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public String getLastUpdate(String reservedDriver) {
        return getValue(RedisKeyParser.taxiDriverLastUpdate(reservedDriver));
    }

    public Location getLocation(String key, String loginId) {
        List<Point> position = redisTemplate.opsForGeo().position(key, loginId);
        if (position == null || position.isEmpty()) throw new LocationNotInRedisException(key, loginId);
        Point point = position.get(0);
        return new Location(point.getY(), point.getX());
    }

    public CallStatus getCallStatus(Long callId) {
        String status = getValue(RedisKeyParser.callStatus(callId));
        return status == null ? CallStatus.DONE : CallStatus.valueOf(status);
    }

    public TaxiDriverStatus getDriverStatus(String driverId) {
        String statusStr = getValue(RedisKeyParser.taxiDriverStatus(driverId));
        return statusStr == null ? TaxiDriverStatus.OFF_DUTY : TaxiDriverStatus.valueOf(statusStr);
    }

    public Set<String> getActiveDrivers() {
        Set<String> members = redisTemplate.opsForSet().members(RedisKeyParser.ACTIVE_TAXI_DRIVER_KEY);
        return members == null ? Set.of() : members;
    }

    public TaxiType getDriversTaxiType(String taxiDriverLoginId) {
        String type = getValue(RedisKeyParser.taxiDriversTaxiType(taxiDriverLoginId));
        if (type == null) throw new RedisKeyNotFoundException(RedisKeyParser.taxiDriversTaxiType(taxiDriverLoginId));
        return TaxiType.valueOf(type);
    }

    public boolean isDriverRejectedForCall(Long callId, String taxiDriverLoginId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(RedisKeyParser.getRejectCallKey(callId), taxiDriverLoginId));
    }

    public Optional<String> getCallIdByDriverId(String driverId) {
        return Optional.ofNullable(getValue(RedisKeyParser.assignCallToDriver(driverId)));
    }

    public boolean hasAssignedCall(String driverId) {
        return redisTemplate.hasKey(RedisKeyParser.assignCallToDriver(driverId));
    }

    public Location getDriverLocation(String driverId) {
        TaxiDriverStatus status = getDriverStatus(driverId);
        TaxiType type = getDriversTaxiType(driverId);
        String key = RedisKeyParser.getTaxiDriverLocationKeyBy(status, type);
        return getLocation(key, driverId);
    }

    public List<String> findNearByAvailableDrivers(Long callId, TaxiType type, double lat, double lon, double radiusKm) {
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(RedisKeyParser.getTaxiDriverLocationKeyBy(TaxiDriverStatus.AVAILABLE, type),
                        new Circle(new Point(lon, lat), new Distance(radiusKm, Metrics.KILOMETERS)));

        if (results == null) return List.of();

        return results.getContent().stream()
                .map(GeoResult::getContent)
                .map(GeoLocation::getName)
                .filter(id -> !isDriverRejectedForCall(callId, id))
                .toList();
    }
}
