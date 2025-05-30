package goorm.humandelivery.driver.application;

import goorm.humandelivery.call.application.port.out.DeleteCallKeyDirectlyPort;
import goorm.humandelivery.call.application.port.out.DeleteCallStatusPort;
import goorm.humandelivery.call.application.port.out.RemoveRejectedDriversForCallPort;
import goorm.humandelivery.driver.application.port.in.DeleteAssignedCallUseCase;
import goorm.humandelivery.driver.application.port.out.DeleteAssignedCallPort;
import goorm.humandelivery.driver.application.port.out.GetAssignedCallPort;
import goorm.humandelivery.global.exception.InvalidCallIdFormatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteAssignedCallService implements DeleteAssignedCallUseCase {

    private final GetAssignedCallPort getAssignedCallPort;
    private final DeleteCallStatusPort deleteCallStatusPort;
    private final DeleteAssignedCallPort deleteAssignedCallPort;
    private final RemoveRejectedDriversForCallPort removeRejectedDriversForCallPort;
    private final DeleteCallKeyDirectlyPort deleteCallKeyDirectlyPort; // 아래에서 따로 설명

    @Override
    public void deleteCallBy(String taxiDriverLoginId) {
        log.info("[DeleteAssignedCallUseCase.deleteCallBy 호출] taxiDriverLoginId : {}", taxiDriverLoginId);
        Optional<String> callIdStr = getAssignedCallPort.getCallIdByDriverId(taxiDriverLoginId);

        if (callIdStr.isEmpty()) {
            log.info("[DeleteAssignedCallUseCase.deleteCallBy] 해당 기사가 가진 콜 정보가 없습니다. taxiDriverId : {}", taxiDriverLoginId);
            return;
        }

        String callIdRaw = callIdStr.get();

        if (!isNumeric(callIdRaw)) {
            log.info("[DeleteAssignedCallUseCase.deleteCallBy] callId가 잘못된 형식입니다. 입력값: {}", callIdRaw);
            throw new InvalidCallIdFormatException(callIdRaw);
        }

        Long callId = Long.parseLong(callIdRaw);

        deleteCallStatusPort.deleteCallStatus(callId);
        deleteAssignedCallPort.deleteAssignedCallOf(taxiDriverLoginId);
        deleteCallKeyDirectlyPort.deleteCallKey(callId);
        removeRejectedDriversForCallPort.removeRejectedDrivers(callId);
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("\\d+");
    }
}