package goorm.humandelivery.call.infrastructure.redis;

import goorm.humandelivery.call.application.port.out.UpdateCallInfoPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.global.exception.CallAlreadyCompletedException;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallInfoRedisAdapter implements UpdateCallInfoPort {

    private final StringRedisTemplate redisTemplate;

    private static final DefaultRedisScript<Long> CANCEL_CALL_SCRIPT;

    static {
        CANCEL_CALL_SCRIPT = new DefaultRedisScript<>();
        CANCEL_CALL_SCRIPT.setScriptText(
                """
                local status = redis.call('GET', KEYS[1])
                if status ~= 'SENT' then
                    return 1 -- 이미 취소 또는 완료된 콜
                end
                redis.call('SET', KEYS[1], 'CANCELLED')
                return 0
                """
        );
        CANCEL_CALL_SCRIPT.setResultType(Long.class);
    }

    @Override
    @Transactional
    public void cancel(CallInfo callInfo) {
        Long callId = callInfo.getId();
        String callStatusKey = RedisKeyParser.callStatus(callId);

        List<String> keys = Collections.singletonList(callStatusKey);
        List<String> args = Collections.emptyList(); // 명시적 선언

        Long result;
        try {
            result = redisTemplate.execute(CANCEL_CALL_SCRIPT, keys, args.toArray());
        } catch (Exception e) {
            log.error("[CallInfoRedisAdapter.cancel] Redis Lua 스크립트 실행 중 예외 발생. callId: {}", callId, e);
            throw new RuntimeException("Redis Lua 스크립트 실행 중 오류", e);
        }

        if (result == null || result != 0L) {
            log.warn("[CallInfoRedisAdapter.cancel] 콜 취소 실패 - 이미 취소되었거나 완료됨. Redis 반환값: {}, callId: {}", result, callId);
            throw new CallAlreadyCompletedException();
        }

        log.info("[CallInfoRedisAdapter.cancel] 콜 취소 성공. callId: {}", callId);
    }
}
