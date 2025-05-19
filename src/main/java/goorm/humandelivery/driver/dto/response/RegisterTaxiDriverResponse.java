package goorm.humandelivery.driver.dto.response;

import goorm.humandelivery.driver.domain.Taxi;
import goorm.humandelivery.driver.domain.TaxiDriver;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RegisterTaxiDriverResponse {

    private String loginId;
    private String name;
    private String licenseCode;
    private String phoneNumber;
    private Taxi taxi;

    private RegisterTaxiDriverResponse(Taxi taxi, String loginId, String name, String licenseCode,
                                       String phoneNumber) {
        this.taxi = taxi;
        this.loginId = loginId;
        this.name = name;
        this.licenseCode = licenseCode;
        this.phoneNumber = phoneNumber;
    }

    public static RegisterTaxiDriverResponse from(TaxiDriver taxiDriver) {
        return new RegisterTaxiDriverResponse(
                taxiDriver.getTaxi(),
                taxiDriver.getLoginId(),
                taxiDriver.getName(),
                taxiDriver.getLicenseCode(),
                taxiDriver.getPhoneNumber()
        );
    }
}
