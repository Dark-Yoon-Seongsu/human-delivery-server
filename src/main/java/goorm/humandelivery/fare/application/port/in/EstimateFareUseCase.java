package goorm.humandelivery.fare.application.port.in;

import goorm.humandelivery.fare.dto.request.EstimateFareRequest;
import goorm.humandelivery.fare.dto.response.EstimateFareResponse;

import java.time.LocalTime;

public interface EstimateFareUseCase {

    EstimateFareResponse estimateFare(EstimateFareRequest estimateFareRequest, LocalTime requestTime);

}
