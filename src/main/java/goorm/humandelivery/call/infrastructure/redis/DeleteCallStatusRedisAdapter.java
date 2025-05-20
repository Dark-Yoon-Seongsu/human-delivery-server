package goorm.humandelivery.call.infrastructure.redis;

import goorm.humandelivery.call.application.port.out.DeleteCallStatusRedisPort;
import goorm.humandelivery.infrastructure.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DeleteCallStatusRedisAdapter implements DeleteCallStatusRedisPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void deleteCallStatus(Long callId) {
        String key = RedisKeyParser.callStatus(callId);
        redisTemplate.delete(key);
    }
}