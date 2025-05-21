package goorm.humandelivery.driver.dto.response;

import goorm.humandelivery.driver.domain.TaxiType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TaxiTypeResponse {

    private TaxiType taxiType;
}
