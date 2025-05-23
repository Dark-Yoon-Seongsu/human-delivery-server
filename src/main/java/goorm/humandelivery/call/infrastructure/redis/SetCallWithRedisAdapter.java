package goorm.humandelivery.call.infrastructure.redis;

import goorm.humandelivery.call.application.port.out.SetCallWithPort;
import goorm.humandelivery.call.domain.CallStatus;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@RequiredArgsConstructor
@Component
public class SetCallWithRedisAdapter implements SetCallWithPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void setCallWith(Long callId, CallStatus callStatus) {
        String key = RedisKeyParser.callStatus(callId);
        redisTemplate.opsForValue().set(key, callStatus.name(), Duration.ofHours(1));
    }
}