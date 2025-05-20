package goorm.humandelivery.call.infrastructure.redis;

import goorm.humandelivery.call.application.port.out.AddRejectedDriverToCallRedisPort;
import goorm.humandelivery.infrastructure.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AddRejectedDriverToCallRedisAdapter implements AddRejectedDriverToCallRedisPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void addRejectedDriverToCall(Long callId, String driverLoginId) {
        String key = RedisKeyParser.getRejectCallKey(callId);
        redisTemplate.opsForSet().add(key, driverLoginId);
    }
}