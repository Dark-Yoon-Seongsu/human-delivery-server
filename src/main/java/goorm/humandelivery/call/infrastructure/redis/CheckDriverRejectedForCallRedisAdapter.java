package goorm.humandelivery.call.infrastructure.redis;

import goorm.humandelivery.call.application.port.out.CheckDriverRejectedForCallPort;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CheckDriverRejectedForCallRedisAdapter implements CheckDriverRejectedForCallPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean isDriverRejected(Long callId, String driverLoginId) {
        String key = RedisKeyParser.getRejectCallKey(callId);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, driverLoginId));
    }
}