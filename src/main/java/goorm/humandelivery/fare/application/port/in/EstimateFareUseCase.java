package goorm.humandelivery.fare.application.port.in;

import goorm.humandelivery.fare.dto.request.EstimateFareRequest;
import goorm.humandelivery.fare.dto.response.EstimateFareResponse;

public interface EstimateFareUseCase {

    EstimateFareResponse estimateFare(EstimateFareRequest estimateFareRequest);

}
