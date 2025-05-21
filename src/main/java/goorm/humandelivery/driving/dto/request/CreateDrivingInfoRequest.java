package goorm.humandelivery.driving.dto.request;

import goorm.humandelivery.shared.location.domain.Location;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDrivingInfoRequest {

    private Long matchingId;
    private Location departPosition;

    public CreateDrivingInfoRequest(Long matchingId, Location departPosition) {
        this.matchingId = matchingId;
        this.departPosition = departPosition;
    }
}
