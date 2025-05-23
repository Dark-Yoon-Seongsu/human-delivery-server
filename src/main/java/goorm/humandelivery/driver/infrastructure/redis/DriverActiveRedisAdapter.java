package goorm.humandelivery.driver.infrastructure.redis;

import goorm.humandelivery.driver.application.port.out.DeleteActiveDriverPort;
import goorm.humandelivery.driver.application.port.out.GetActiveDriversPort;
import goorm.humandelivery.driver.application.port.out.SetActiveDriverPort;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DriverActiveRedisAdapter implements SetActiveDriverPort, GetActiveDriversPort, DeleteActiveDriverPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void setActiveDriver(String driverLoginId) {
        redisTemplate.opsForSet().add(RedisKeyParser.ACTIVE_TAXI_DRIVER_KEY, driverLoginId);
    }

    @Override
    public void setOffDuty(String taxiDriverLoginId) {
        redisTemplate.opsForSet().remove(RedisKeyParser.ACTIVE_TAXI_DRIVER_KEY, taxiDriverLoginId);
    }

    @Override
    public Set<String> getActiveDrivers() {
        Set<String> members = redisTemplate.opsForSet().members(RedisKeyParser.ACTIVE_TAXI_DRIVER_KEY);
        return members == null ? Set.of() : members;
    }
}
