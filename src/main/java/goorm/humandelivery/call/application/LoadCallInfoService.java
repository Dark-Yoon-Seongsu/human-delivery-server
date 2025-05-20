package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.in.GetCallAcceptResponseUseCase;
import goorm.humandelivery.call.application.port.in.GetCustomerLoginIdByCallIdUseCase;
import goorm.humandelivery.call.application.port.out.LoadCallInfoPort;
import goorm.humandelivery.call.dto.response.CallAcceptResponse;
import goorm.humandelivery.global.exception.CallInfoEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoadCallInfoService implements GetCallAcceptResponseUseCase, GetCustomerLoginIdByCallIdUseCase {

    private final LoadCallInfoPort loadCallInfoPort;

    @Override
    public CallAcceptResponse getCallAcceptResponse(Long callId) {
        return loadCallInfoPort.findCallInfoAndCustomerByCallId(callId)
                .orElseThrow(CallInfoEntityNotFoundException::new);
    }

    @Override
    public String findCustomerLoginIdByCallId(Long callId) {
        return loadCallInfoPort.findCustomerLoginIdByCallId(callId)
                .orElseThrow(CallInfoEntityNotFoundException::new);
    }
}
