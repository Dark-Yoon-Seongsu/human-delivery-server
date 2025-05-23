package goorm.humandelivery.call.infrastructure.redis;

import goorm.humandelivery.call.application.port.out.DeleteCallStatusPort;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DeleteCallStatusRedisAdapter implements DeleteCallStatusPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void deleteCallStatus(Long callId) {
        String key = RedisKeyParser.callStatus(callId);
        redisTemplate.delete(key);
    }
}