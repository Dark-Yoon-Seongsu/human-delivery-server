package goorm.humandelivery.customer.application.port.in;

import goorm.humandelivery.customer.dto.request.LoginCustomerRequest;
import goorm.humandelivery.customer.dto.response.LoginCustomerResponse;

public interface LoginCustomerUseCase {

    LoginCustomerResponse authenticateAndGenerateToken(LoginCustomerRequest loginCustomerRequest);

}
