package goorm.humandelivery.fare.dto.response;

import goorm.humandelivery.driver.domain.TaxiType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@AllArgsConstructor
public class EstimateFareResponse {

    private Map<TaxiType, BigDecimal> estimatedFares;
    private BigDecimal kakaoEstimatedFare;

}
