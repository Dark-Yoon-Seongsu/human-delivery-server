package goorm.humandelivery.call.application.port.in;

import goorm.humandelivery.call.dto.request.CallMessageRequest;

public interface RequestCallUseCase {

    void requestCall(CallMessageRequest request, String customerLoginId);

}