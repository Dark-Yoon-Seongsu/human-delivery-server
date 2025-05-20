package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.dto.response.CallAcceptResponse;

import java.util.Optional;

public interface LoadCallInfoPort {

    Optional<CallInfo> findById(Long callId);

    Optional<CallAcceptResponse> findCallInfoAndCustomerByCallId(Long callId);

    Optional<String> findCustomerLoginIdByCallId(Long callId);

}
