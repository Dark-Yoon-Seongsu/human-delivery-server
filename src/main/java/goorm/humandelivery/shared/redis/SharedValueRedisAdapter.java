package goorm.humandelivery.shared.redis;

import goorm.humandelivery.shared.application.port.out.GetValuePort;
import goorm.humandelivery.shared.application.port.out.SetValueWithTtlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SharedValueRedisAdapter implements SetValueWithTtlPort, GetValuePort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void setValueWithTTL(String key, String value, java.time.Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

}