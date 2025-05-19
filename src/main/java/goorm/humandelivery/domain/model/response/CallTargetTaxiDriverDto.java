package goorm.humandelivery.domain.model.response;

import goorm.humandelivery.driver.domain.TaxiDriver;
import lombok.Getter;

@Getter
public class CallTargetTaxiDriverDto {

    private final Long driverId;
    private final String driverLoginId;

    public CallTargetTaxiDriverDto(Long driverId, String driverLoginId) {
        this.driverId = driverId;
        this.driverLoginId = driverLoginId;
    }

    public static CallTargetTaxiDriverDto from(TaxiDriver taxiDriver) {
        return new CallTargetTaxiDriverDto(taxiDriver.getId(), taxiDriver.getLoginId());
    }
}
