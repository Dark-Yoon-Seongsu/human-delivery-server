package goorm.humandelivery.application;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goorm.humandelivery.common.exception.CallInfoEntityNotFoundException;
import goorm.humandelivery.domain.model.response.CallAcceptResponse;
import goorm.humandelivery.domain.repository.CallInfoRepository;

@Service
@RequiredArgsConstructor

public class CallInfoService {

	private final CallInfoRepository callInfoRepository;

	@Transactional(readOnly = true)
	public CallAcceptResponse getCallAcceptResponse(Long callId) {
		return callInfoRepository.findCallInfoAndCustomerByCallId(callId)
			.orElseThrow(CallInfoEntityNotFoundException::new);
	}

	@Transactional(readOnly = true)
	public String findCustomerLoginIdById(Long id) {
		return callInfoRepository.findCustomerLoginIdByCallId(id).orElseThrow(CallInfoEntityNotFoundException::new);
	}

	@Transactional
	public void deleteCallById(Long callId) {
		callInfoRepository.deleteById(callId);
	}
}
