package goorm.humandelivery.fare.dto.request;

import goorm.humandelivery.shared.location.domain.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EstimateFareRequest {

    private Location expectedOrigin;
    private Location expectedDestination;
    private LocalTime requestTime;

}
