package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.shared.dto.response.ErrorResponse;

public interface SendDispatchFailToCustomerPort {

    void sendToCustomer(String customerLoginId, ErrorResponse response);

}