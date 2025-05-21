package goorm.humandelivery.call.infrastructure.redis;

import goorm.humandelivery.call.application.port.out.DeleteCallKeyDirectlyRedisPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteCallKeyDirectlyRedisAdapter implements DeleteCallKeyDirectlyRedisPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void deleteCallKey(Long callId) {
        redisTemplate.delete(String.valueOf(callId));
    }
}
