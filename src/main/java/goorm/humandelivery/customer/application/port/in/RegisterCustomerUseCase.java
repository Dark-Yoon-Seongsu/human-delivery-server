package goorm.humandelivery.customer.application.port.in;

import goorm.humandelivery.customer.dto.request.RegisterCustomerRequest;
import goorm.humandelivery.customer.dto.response.RegisterCustomerResponse;

public interface RegisterCustomerUseCase {

    RegisterCustomerResponse register(RegisterCustomerRequest registerCustomerRequest);

}