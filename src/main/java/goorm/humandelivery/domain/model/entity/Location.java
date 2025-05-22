package goorm.humandelivery.domain.model.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
public class Location {

	@NotNull
	private Double latitude;   // 위도

	@NotNull
	private Double longitude;  // 경도

	@Builder
	public Location(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
