package goorm.humandelivery.driving.application.port.out;

import goorm.humandelivery.driving.dto.response.DrivingInfoResponse;

public interface SendDrivingStartToCustomerPort {

    void sendToCustomer(String customerLoginId, DrivingInfoResponse response);

}