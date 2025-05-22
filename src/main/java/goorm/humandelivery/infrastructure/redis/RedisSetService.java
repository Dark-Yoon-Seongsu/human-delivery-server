package goorm.humandelivery.infrastructure.redis;

import goorm.humandelivery.domain.model.entity.CallStatus;
import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.infrastructure.redis.key.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import goorm.humandelivery.domain.model.entity.Location;
import org.springframework.data.geo.Point;


import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisSetService {

    private final StringRedisTemplate redisTemplate;

    public void setValueWithTTL(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public void setValueWithTTLIfAbsent(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
    }

    public void setLocation(String key, String loginId, Location location) {
        redisTemplate.opsForGeo().add(key, new Point(location.getLongitude(), location.getLatitude()), loginId);
    }

    public void setCallWith(Long callId, CallStatus callStatus) {
        redisTemplate.opsForValue().set(RedisKeyParser.callStatus(callId), callStatus.name(), Duration.ofHours(1));
    }

    public void setDriversStatus(String taxiDriverLoginId, TaxiDriverStatus status) {
        redisTemplate.opsForValue().set(RedisKeyParser.taxiDriverStatus(taxiDriverLoginId), status.name(), Duration.ofHours(1));
    }

    public void setActive(String taxiDriverLoginId) {
        redisTemplate.opsForSet().add(RedisKeyParser.ACTIVE_TAXI_DRIVER_KEY, taxiDriverLoginId);
    }

    public void setDriversTaxiType(String taxiDriverLoginId, TaxiType taxiType) {
        redisTemplate.opsForValue().setIfAbsent(RedisKeyParser.taxiDriversTaxiType(taxiDriverLoginId), taxiType.name(), Duration.ofDays(1));
    }

    public void assignCallToDriver(Long callId, String taxiDriverLoginId) {
        redisTemplate.opsForValue().set(RedisKeyParser.assignCallToDriver(taxiDriverLoginId), String.valueOf(callId));
    }

    public void addRejectedDriverToCall(Long callId, String taxiDriverLoginId) {
        redisTemplate.opsForSet().add(RedisKeyParser.getRejectCallKey(callId), taxiDriverLoginId);
    }
}
