package goorm.humandelivery.driving.application.port.out;

import goorm.humandelivery.driving.dto.response.DrivingSummaryResponse;

public interface SendDrivingCompletedToCustomerPort {

    void sendToCustomer(String customerLoginId, DrivingSummaryResponse response);

}