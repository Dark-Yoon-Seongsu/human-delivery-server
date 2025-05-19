package goorm.humandelivery.domain.model.request;

import goorm.humandelivery.common.util.annotation.ValidEnum;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 택시 상태변경
 */
@Getter
@Setter
public class UpdateTaxiDriverStatusRequest {
	@ValidEnum(enumClass = TaxiDriverStatus.class, message = "유효하지 않은 TaxiDriverStatus 입니다. 상태 목록 : 미운행, 빈차, 예약, 배달중")
	String status;

}
