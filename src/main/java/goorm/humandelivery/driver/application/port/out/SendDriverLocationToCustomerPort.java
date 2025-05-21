package goorm.humandelivery.driver.application.port.out;

import goorm.humandelivery.driver.dto.response.DriverLocationResponse;

public interface SendDriverLocationToCustomerPort {

    void sendToCustomer(String customerLoginId, DriverLocationResponse response);

}