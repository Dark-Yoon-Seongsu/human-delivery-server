package goorm.humandelivery.shared.location.infrastructure.redis;

import goorm.humandelivery.global.exception.LocationNotInRedisException;
import goorm.humandelivery.shared.location.application.port.out.GetLocationPort;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class GetLocationRedisAdapter implements GetLocationPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public Location getLocation(String key, String loginId) {
        List<Point> position = redisTemplate.opsForGeo().position(key, loginId);

        if (position == null) {
            throw new LocationNotInRedisException(key, loginId);
        }

        Point point = position
                .stream()
                .findFirst()
                .orElseThrow(() -> new LocationNotInRedisException(key, loginId));

        return new Location(point.getY(), point.getX()); // 위도, 경도 순서
    }
}