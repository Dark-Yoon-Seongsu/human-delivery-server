package goorm.humandelivery.domain.model.request;

import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.driver.domain.TaxiType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NearbyDriversRequest {

	/**
	 * TODO -> location으로 변경
	 */

	@NotNull
	private Location location;

	@NotNull
	private Double radiusInKm;

	@NotNull
	private TaxiType taxiType;



}
