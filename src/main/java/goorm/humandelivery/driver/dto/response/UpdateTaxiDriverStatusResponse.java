package goorm.humandelivery.driver.dto.response;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 택시 상태변경
 */
@Getter
@Setter
public class UpdateTaxiDriverStatusResponse {

    // 요청 결과 이후 택시 상태
    TaxiDriverStatus taxiDriverStatus;

    public UpdateTaxiDriverStatusResponse() {}

    public UpdateTaxiDriverStatusResponse(TaxiDriverStatus taxiDriverStatus) {
        this.taxiDriverStatus = taxiDriverStatus;
    }
}
