package goorm.humandelivery.call.infrastructure.redis;

import goorm.humandelivery.call.application.port.out.RemoveRejectedDriversForCallRedisPort;
import goorm.humandelivery.infrastructure.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RemoveRejectedDriversForCallRedisAdapter implements RemoveRejectedDriversForCallRedisPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void removeRejectedDrivers(Long callId) {
        String key = RedisKeyParser.getRejectCallKey(callId);
        redisTemplate.delete(key);
    }
}