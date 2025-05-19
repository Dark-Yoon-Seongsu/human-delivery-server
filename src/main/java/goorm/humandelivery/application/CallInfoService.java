package goorm.humandelivery.application;

import goorm.humandelivery.domain.model.response.CallAcceptResponse;
import goorm.humandelivery.domain.repository.CallInfoRepository;
import goorm.humandelivery.global.exception.CallInfoEntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CallInfoService {

    private final CallInfoRepository callInfoRepository;

    @Autowired
    public CallInfoService(CallInfoRepository callInfoRepository) {
        this.callInfoRepository = callInfoRepository;
    }

    public CallAcceptResponse getCallAcceptResponse(Long callId) {
        return callInfoRepository.findCallInfoAndCustomerByCallId(callId)
                .orElseThrow(CallInfoEntityNotFoundException::new);
    }

    public String findCustomerLoginIdById(Long id) {
        return callInfoRepository.findCustomerLoginIdByCallId(id).orElseThrow(CallInfoEntityNotFoundException::new);
    }

    @Transactional
    public void deleteCallById(Long callId) {
        callInfoRepository.deleteById(callId);
    }
}
