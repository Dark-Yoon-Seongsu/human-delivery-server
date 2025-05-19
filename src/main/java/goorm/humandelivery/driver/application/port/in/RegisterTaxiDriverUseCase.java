package goorm.humandelivery.driver.application.port.in;

import goorm.humandelivery.driver.dto.request.RegisterTaxiDriverRequest;
import goorm.humandelivery.driver.dto.response.RegisterTaxiDriverResponse;

public interface RegisterTaxiDriverUseCase {

    RegisterTaxiDriverResponse register(RegisterTaxiDriverRequest registerTaxiDriverRequest);

}
