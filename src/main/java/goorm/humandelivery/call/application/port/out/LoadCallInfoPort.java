package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.call.domain.CallInfo;

import java.util.Optional;

public interface LoadCallInfoPort {

    Optional<CallInfo> findCallInfoById(Long callId);

    Optional<CallInfo> findCallInfoAndCustomerByCallId(Long callId);

    Optional<String> findCustomerLoginIdByCallId(Long callId);

}
