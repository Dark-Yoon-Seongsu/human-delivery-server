package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.shared.dto.response.ErrorResponse;

public interface SendDispatchFailToDriverPort {

    void sendToDriver(String driverLoginId, ErrorResponse response);

}