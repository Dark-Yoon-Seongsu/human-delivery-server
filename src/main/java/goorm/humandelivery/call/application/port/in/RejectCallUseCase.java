package goorm.humandelivery.call.application.port.in;

import goorm.humandelivery.call.dto.response.CallRejectResponse;

public interface RejectCallUseCase {

    CallRejectResponse addRejectedDriverToCall(Long callId, String taxiDriverLoginId);

}
