package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.in.RejectCallUseCase;
import goorm.humandelivery.call.application.port.out.AddRejectedDriverToCallPort;
import goorm.humandelivery.call.dto.response.CallRejectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RejectCallService implements RejectCallUseCase {

    private final AddRejectedDriverToCallPort addRejectedDriverToCallPort;

    @Override
    public CallRejectResponse addRejectedDriverToCall(Long callId, String taxiDriverLoginId) {
        log.info("[RejectCallService.addRejectedDriverToCall] 콜 거절.  콜 ID : {}, 택시기사 ID : {}", callId, taxiDriverLoginId);

        // 해당 콜을 거절한 택시기사 집합에 추가
        addRejectedDriverToCallPort.addRejectedDriverToCall(callId, taxiDriverLoginId);
        return new CallRejectResponse(callId);
    }
}
