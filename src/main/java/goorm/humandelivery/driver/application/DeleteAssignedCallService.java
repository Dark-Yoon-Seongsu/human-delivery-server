package goorm.humandelivery.driver.application;

import goorm.humandelivery.call.application.port.out.DeleteCallKeyDirectlyRedisPort;
import goorm.humandelivery.call.application.port.out.DeleteCallStatusRedisPort;
import goorm.humandelivery.call.application.port.out.RemoveRejectedDriversForCallRedisPort;
import goorm.humandelivery.driver.application.port.in.DeleteAssignedCallUseCase;
import goorm.humandelivery.driver.application.port.out.DeleteAssignedCallRedisPort;
import goorm.humandelivery.driver.application.port.out.GetAssignedCallRedisPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteAssignedCallService implements DeleteAssignedCallUseCase {

    private final GetAssignedCallRedisPort getAssignedCallRedisPort;
    private final DeleteCallStatusRedisPort deleteCallStatusRedisPort;
    private final DeleteAssignedCallRedisPort deleteAssignedCallRedisPort;
    private final RemoveRejectedDriversForCallRedisPort removeRejectedDriversForCallPort;
    private final DeleteCallKeyDirectlyRedisPort deleteCallKeyDirectlyRedisPort; // 아래에서 따로 설명

    @Override
    public void deleteCallBy(String taxiDriverLoginId) {
        log.info("[DeleteAssignedCallUseCase.deleteCallBy 호출] taxiDriverLoginId : {}", taxiDriverLoginId);
        Optional<String> callIdStr = getAssignedCallRedisPort.getCallIdByDriverId(taxiDriverLoginId);

        if (callIdStr.isEmpty()) {
            log.info("[DeleteAssignedCallUseCase.deleteCallBy 호출] 해당 기사가 가진 콜 정보가 없습니다. taxiDriverId : {}", taxiDriverLoginId);
            return;
        }

        Long callId = Long.parseLong(callIdStr.get());

        deleteCallStatusRedisPort.deleteCallStatus(callId);
        deleteAssignedCallRedisPort.deleteAssignedCallOf(taxiDriverLoginId);
        deleteCallKeyDirectlyRedisPort.deleteCallKey(callId); // callId 자체를 key로 삭제
        removeRejectedDriversForCallPort.removeRejectedDrivers(callId);
    }
}
