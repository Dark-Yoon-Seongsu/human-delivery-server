package goorm.humandelivery.driver.dto.request;

import goorm.humandelivery.driver.domain.FuelType;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.shared.annotation.ValidEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterTaxiRequest {

    private String model;

    @ValidEnum(enumClass = TaxiType.class, message = "지원하지 않는 택시 타입입니다.")
    private String taxiType;

    private String plateNumber;

    @ValidEnum(enumClass = FuelType.class, message = "지원하지 않는 연료 타입입니다.")
    private String fuelType;
}
