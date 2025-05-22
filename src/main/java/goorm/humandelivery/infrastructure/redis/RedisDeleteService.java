package goorm.humandelivery.infrastructure.redis;

import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.infrastructure.redis.key.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisDeleteService {

    private final StringRedisTemplate redisTemplate;
    private final RedisGetService redisGetService;

    public void setOffDuty(String driverId) {
        redisTemplate.opsForSet().remove(RedisKeyParser.ACTIVE_TAXI_DRIVER_KEY, driverId);
    }

    public void removeFromLocation(String driverId, TaxiType type, TaxiDriverStatus status) {
        redisTemplate.opsForZSet().remove(RedisKeyParser.getTaxiDriverLocationKeyBy(status, type), driverId);
    }

    public void deleteCallStatus(Long callId) {
        redisTemplate.delete(RedisKeyParser.callStatus(callId));
    }

    public void deleteAssignedCallOf(String driverId) {
        redisTemplate.delete(RedisKeyParser.assignCallToDriver(driverId));
    }

    public void removeRejectedDriversForCall(Long callId) {
        redisTemplate.delete(RedisKeyParser.getRejectCallKey(callId));
    }

    public void deleteCallBy(String driverId) {
        redisGetService.getCallIdByDriverId(driverId).ifPresent(callIdStr -> {
            Long callId = Long.valueOf(callIdStr);
            deleteCallStatus(callId);
            deleteAssignedCallOf(driverId);
            redisTemplate.delete(String.valueOf(callId));
            removeRejectedDriversForCall(callId);
        });
    }

    public void deleteAllLocationDataInRedis(String driverId, TaxiType taxiType) {
        for (TaxiDriverStatus status : TaxiDriverStatus.values()) {
            removeFromLocation(driverId, taxiType, status);
        }
    }
}
