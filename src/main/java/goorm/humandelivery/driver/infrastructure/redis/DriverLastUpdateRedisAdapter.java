package goorm.humandelivery.driver.infrastructure.redis;

import goorm.humandelivery.driver.application.port.out.GetDriverLastUpdatePort;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DriverLastUpdateRedisAdapter implements GetDriverLastUpdatePort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public String getLastUpdate(String driverLoginId) {
        String key = RedisKeyParser.taxiDriverLastUpdate(driverLoginId);
        return redisTemplate.opsForValue().get(key);
    }
}