package goorm.humandelivery.infrastructure.redis;

import goorm.humandelivery.infrastructure.redis.key.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisAtomicCallService {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean atomicAcceptCall(Long callId, String driverLoginId) {
        String callStatusKey = RedisKeyParser.callStatus(callId);
        String driverCallKey = RedisKeyParser.assignCallToDriver(driverLoginId);
        String driverStatusKey = RedisKeyParser.taxiDriverStatus(driverLoginId);
        String lockKey = String.valueOf(callId); // Lock 용도

        String lua = """
            local callStatus = redis.call('GET', KEYS[1])
            local driverCall = redis.call('EXISTS', KEYS[2])
            local driverStatus = redis.call('GET', KEYS[3])
            
            if callStatus ~= ARGV[1] then
                return 1
            end
            
            if driverCall == 1 then
                return 2
            end
            
            if driverStatus ~= ARGV[2] then
                return 3
            end
            
            local success = redis.call('SETNX', KEYS[4], ARGV[3])
            if success == 0 then
                return 4
            end

            redis.call('SET', KEYS[1], ARGV[4])
            redis.call('SET', KEYS[3], ARGV[5])
            redis.call('SET', KEYS[2], ARGV[6])
            return 0
        """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(lua);
        script.setResultType(Long.class);

        List<String> keys = List.of(callStatusKey, driverCallKey, driverStatusKey, lockKey);
        List<String> args = List.of(
                "SENT", "AVAILABLE", driverLoginId,
                "DONE", "RESERVED", String.valueOf(callId)
        );

        Long result = redisTemplate.execute(script, keys, args.toArray());
        return result != null && result == 0L;
    }
}
