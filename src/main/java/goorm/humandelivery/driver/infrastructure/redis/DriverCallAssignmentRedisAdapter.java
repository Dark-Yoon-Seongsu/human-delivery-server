package goorm.humandelivery.driver.infrastructure.redis;

import goorm.humandelivery.driver.application.port.out.DeleteAssignedCallPort;
import goorm.humandelivery.driver.application.port.out.GetAssignedCallPort;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DriverCallAssignmentRedisAdapter implements GetAssignedCallPort, DeleteAssignedCallPort {

    private final StringRedisTemplate redisTemplate;

    // 기사에게 할당된 콜 ID을 Optional<String>으로 반환
    @Override
    public Optional<String> getCallIdByDriverId(String driverLoginId) {
        String key = RedisKeyParser.assignCallToDriver(driverLoginId);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    // 기사에게 할당된 콜 키를 삭제
    @Override
    public void deleteAssignedCallOf(String driverLoginId) {
        String key = RedisKeyParser.assignCallToDriver(driverLoginId);
        redisTemplate.delete(key);
    }
}
