package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.in.DeleteMatchingUseCase;
import goorm.humandelivery.call.application.port.out.DeleteMatchingPort;
import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.global.exception.MatchingEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteMatchingService implements DeleteMatchingUseCase {

    private final LoadMatchingPort loadMatchingPort;
    private final DeleteMatchingPort deleteMatchingPort;

    @Override
    public void deleteByCallId(Long callId) {
        Matching matching = loadMatchingPort.findMatchingByCallInfoId(callId)
                .orElseThrow(MatchingEntityNotFoundException::new);

        deleteMatchingPort.delete(matching);
    }
}
