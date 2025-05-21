package goorm.humandelivery.driver.dto.response;

import goorm.humandelivery.shared.location.domain.Location;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverLocationResponse {

    private Location location;

    public DriverLocationResponse() {
    }

    public DriverLocationResponse(Location location) {
        this.location = location;
    }
}
