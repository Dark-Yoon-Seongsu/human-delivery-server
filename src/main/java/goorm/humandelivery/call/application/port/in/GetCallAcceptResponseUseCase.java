package goorm.humandelivery.call.application.port.in;

import goorm.humandelivery.call.dto.response.CallAcceptResponse;

public interface GetCallAcceptResponseUseCase {

    CallAcceptResponse getCallAcceptResponse(Long callId);

}
