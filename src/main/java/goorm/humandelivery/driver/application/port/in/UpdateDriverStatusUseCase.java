package goorm.humandelivery.driver.application.port.in;

import goorm.humandelivery.driver.dto.request.UpdateTaxiDriverStatusRequest;
import goorm.humandelivery.driver.dto.response.UpdateTaxiDriverStatusResponse;

public interface UpdateDriverStatusUseCase {

    UpdateTaxiDriverStatusResponse updateStatus(UpdateTaxiDriverStatusRequest request, String driverLoginId);

}