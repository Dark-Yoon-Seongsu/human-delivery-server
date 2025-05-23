package goorm.humandelivery.shared.location.infrastructure.redis;

import goorm.humandelivery.shared.location.application.port.out.SetLocationPort;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SetLocationRedisAdapter implements SetLocationPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void setLocation(String key, String loginId, Location location) {
        redisTemplate.opsForGeo().add(key, new Point(location.getLongitude(), location.getLatitude()), loginId);
    }
}