package goorm.humandelivery.domain.model.response;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchingSuccessResponse {

	private TaxiDriverStatus taxiDriverStatus;
	private String taxiDriverLoginId;


}
