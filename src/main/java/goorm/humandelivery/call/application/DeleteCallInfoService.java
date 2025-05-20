package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.in.DeleteCallInfoUseCase;
import goorm.humandelivery.call.application.port.out.DeleteCallInfoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteCallInfoService implements DeleteCallInfoUseCase {

    private final DeleteCallInfoPort deleteCallInfoPort;

    @Override
    public void deleteCallById(Long callId) {
        deleteCallInfoPort.deleteById(callId);
    }

}
