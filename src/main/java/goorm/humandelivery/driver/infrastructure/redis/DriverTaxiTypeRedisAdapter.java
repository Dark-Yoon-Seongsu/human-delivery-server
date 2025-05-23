package goorm.humandelivery.driver.infrastructure.redis;

import goorm.humandelivery.driver.application.port.out.GetDriverTaxiTypePort;
import goorm.humandelivery.driver.application.port.out.SetDriverTaxiTypePort;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.global.exception.RedisKeyNotFoundException;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class DriverTaxiTypeRedisAdapter implements SetDriverTaxiTypePort, GetDriverTaxiTypePort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void setDriverTaxiType(String taxiDriverLoginId, TaxiType taxiType) {
        String key = RedisKeyParser.taxiDriversTaxiType(taxiDriverLoginId);
        redisTemplate.opsForValue().setIfAbsent(key, taxiType.name(), Duration.ofDays(1));
    }

    @Override
    public TaxiType getDriverTaxiType(String taxiDriverLoginId) {
        String key = RedisKeyParser.taxiDriversTaxiType(taxiDriverLoginId);
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            throw new RedisKeyNotFoundException(key);
        }

        return TaxiType.valueOf(value);
    }
}
