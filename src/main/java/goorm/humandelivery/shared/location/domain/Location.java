package goorm.humandelivery.shared.location.domain;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    @NotNull
    private Double latitude;   // 위도

    @NotNull
    private Double longitude;  // 경도


}
