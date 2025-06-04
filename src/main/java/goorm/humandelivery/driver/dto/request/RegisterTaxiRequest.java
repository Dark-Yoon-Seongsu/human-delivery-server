package goorm.humandelivery.driver.dto.request;

import goorm.humandelivery.driver.domain.FuelType;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.shared.annotation.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterTaxiRequest {

    @NotBlank(message = "차종을 입력해 주세요.")
    private String model;

    @ValidEnum(enumClass = TaxiType.class, message = "지원하지 않는 택시 타입입니다.")
    private String taxiType;

    @NotBlank(message = "차량번호를 입력해 주세요.")
    private String plateNumber;

    @ValidEnum(enumClass = FuelType.class, message = "지원하지 않는 연료 타입입니다.")
    private String fuelType;
}
