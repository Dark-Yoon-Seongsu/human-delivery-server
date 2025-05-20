package goorm.humandelivery.call.application.port.in;

import goorm.humandelivery.call.dto.request.CallAcceptRequest;
import goorm.humandelivery.call.dto.response.CallAcceptResponse;

public interface AcceptCallUseCase {

    CallAcceptResponse acceptCall(CallAcceptRequest callAcceptRequest, String taxiDriverLoginId);

}
