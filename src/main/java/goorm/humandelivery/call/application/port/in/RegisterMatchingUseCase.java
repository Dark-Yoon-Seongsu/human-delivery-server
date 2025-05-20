package goorm.humandelivery.call.application.port.in;

import goorm.humandelivery.call.dto.request.CreateMatchingRequest;

public interface RegisterMatchingUseCase {

    void create(CreateMatchingRequest createMatchingRequest);

}
