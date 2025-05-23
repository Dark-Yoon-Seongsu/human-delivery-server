package goorm.humandelivery.shared.location.infrastructure.redis;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import goorm.humandelivery.shared.location.application.port.out.RemoveFromLocationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RemoveFromLocationRedisAdapter implements RemoveFromLocationPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void removeFromLocation(String driverLoginId, TaxiType taxiType, TaxiDriverStatus status) {
        String key = RedisKeyParser.getTaxiDriverLocationKeyBy(status, taxiType);
        redisTemplate.opsForZSet().remove(key, driverLoginId);
    }
}