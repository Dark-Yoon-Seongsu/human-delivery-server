package goorm.humandelivery.call.application.port.in;

public interface GetCustomerLoginIdByCallIdUseCase {

    String findCustomerLoginIdByCallId(Long callId);

}
