package goorm.humandelivery.driver.application.port.in;

import goorm.humandelivery.driver.dto.request.UpdateDriverLocationRequest;

public interface UpdateDriverLocationUseCase {

    void updateLocation(UpdateDriverLocationRequest request, String taxiDriverLoginId);

}