package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.call.dto.response.MatchingSuccessResponse;

public interface NotifyDispatchSuccessToCustomerPort {

    void sendToCustomer(String customerLoginId, MatchingSuccessResponse response);

}