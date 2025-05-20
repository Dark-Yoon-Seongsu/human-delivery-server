package goorm.humandelivery.domain.model.response;

import goorm.humandelivery.domain.model.entity.Location;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationResponse {

    private Location location;

    public LocationResponse() {
    }

    public LocationResponse(Location location) {
        this.location = location;
    }
}
