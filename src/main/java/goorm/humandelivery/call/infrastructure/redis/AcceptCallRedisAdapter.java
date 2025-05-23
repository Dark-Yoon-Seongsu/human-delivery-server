package goorm.humandelivery.call.infrastructure.redis;

import goorm.humandelivery.call.application.port.out.AcceptCallPort;
import goorm.humandelivery.global.exception.CallAlreadyCompletedException;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptCallRedisAdapter implements AcceptCallPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void atomicAcceptCall(Long callId, String driverLoginId) {
        String callStatusKey = RedisKeyParser.callStatus(callId); // e.g. call:123:status
        String driverCallKey = RedisKeyParser.assignCallToDriver(driverLoginId);  // e.g. taxidriver:driver001@example.com:call
        String driverStatusKey = RedisKeyParser.taxiDriverStatus(driverLoginId); // e.g. taxidriver:driver001@example.com:status

        String lockKey = String.valueOf(callId);

        List<String> keys = List.of(callStatusKey, driverCallKey, driverStatusKey, lockKey);
        List<String> args = List.of("SENT", "AVAILABLE", driverLoginId, "DONE", String.valueOf(callId), "RESERVED");
        String lua = """
                      local callStatus = redis.call('GET', KEYS[1])
                      local driverCall = redis.call('EXISTS', KEYS[2])
                      local driverStatus = redis.call('GET', KEYS[3])
                
                      if callStatus ~= ARGV[1] then
                          return 1 -- 콜 상태가 SENT 아님
                      end
                
                      if driverCall == 1 then
                          return 2 -- 이미 기사에게 콜 할당됨
                      end
                
                      if driverStatus ~= ARGV[2] then
                          return 3 -- 기사 상태가 AVAILABLE 아님
                      end
                
                      local success = redis.call('SETNX', KEYS[4], ARGV[3])
                      if success == 0 then
                          return 4 -- SETNX 실패 (다른 사람이 먼저 수락)
                      end
                
                      -- 여기서 상태 변화까지 시켜야 한다.
                    redis.call('SET', KEYS[1], ARGV[4]) -- callStatus = DONE
                	redis.call('SET', KEYS[2], ARGV[5]) -- driverCall = callId
                	redis.call('SET', KEYS[3], ARGV[6]) -- driverStatus = RESERVED
                      return 0;
                """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(lua);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, keys, args.toArray());

        if (!(result != null && result == 0L)) {
            log.info("[acceptTaxiCall.CallAcceptResponse] 완료된 콜에 대한 배차 신청. 택시기사 : {}, 콜ID : {}", driverLoginId, callId);
            throw new CallAlreadyCompletedException();
        }
    }

}
