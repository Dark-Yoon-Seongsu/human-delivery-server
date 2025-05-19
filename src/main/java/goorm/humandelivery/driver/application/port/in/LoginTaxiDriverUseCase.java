package goorm.humandelivery.driver.application.port.in;

import goorm.humandelivery.driver.dto.request.LoginTaxiDriverRequest;
import goorm.humandelivery.shared.auth.JwtResponse;

public interface LoginTaxiDriverUseCase {

    JwtResponse login(LoginTaxiDriverRequest request);

}
