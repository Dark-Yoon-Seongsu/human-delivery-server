package goorm.humandelivery.TestFixture;

import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.dto.request.RegisterCustomerRequest;
import goorm.humandelivery.customer.dto.request.LoginCustomerRequest;
import goorm.humandelivery.customer.dto.response.LoginCustomerResponse;

public class CustomerTestFixture {

	public static Customer createCustomerEntity(String loginId, String password, String name, String phoneNumber) {
		return Customer.builder()
			.loginId(loginId)
			.password(password)
			.name(name)
			.phoneNumber(phoneNumber)
			.build();
	}

	public static RegisterCustomerRequest createCreateCustomerRequest(String loginId, String password, String name, String phoneNumber) {
		return new RegisterCustomerRequest(loginId, password, name, phoneNumber);
	}
	public static LoginCustomerRequest createLoginCustomerRequest(String loginId, String password) {
		return new LoginCustomerRequest(loginId, password);
	}
	public static LoginCustomerResponse createLoginCustomerResponse(String accessToken) {
		return new LoginCustomerResponse(accessToken);
	}
}
