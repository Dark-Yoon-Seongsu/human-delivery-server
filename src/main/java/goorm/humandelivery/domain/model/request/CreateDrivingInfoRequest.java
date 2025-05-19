package goorm.humandelivery.domain.model.request;

import goorm.humandelivery.domain.model.entity.Location;
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
