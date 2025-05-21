package goorm.humandelivery.driver.infrastructure.redis;

import goorm.humandelivery.driver.application.port.out.GetDriverStatusRedisPort;
import goorm.humandelivery.driver.application.port.out.SetDriverStatusRedisPort;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class DriverStatusRedisAdapter implements
        SetDriverStatusRedisPort,
        GetDriverStatusRedisPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void setDriverStatus(String driverLoginId, TaxiDriverStatus status) {
        String key = RedisKeyParser.taxiDriverStatus(driverLoginId);
        redisTemplate.opsForValue().set(key, status.name(), Duration.ofHours(1));
    }

    @Override
    public TaxiDriverStatus getDriverStatus(String driverId) {
        String key = RedisKeyParser.taxiDriverStatus(driverId);
        String statusStr = redisTemplate.opsForValue().get(key);

        if (statusStr == null) {
            return TaxiDriverStatus.OFF_DUTY;
        }
        return TaxiDriverStatus.valueOf(statusStr);
    }
}
