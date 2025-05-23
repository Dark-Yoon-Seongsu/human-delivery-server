package goorm.humandelivery.shared.location.application.port.out;

import goorm.humandelivery.shared.location.domain.Location;

public interface GetLocationPort {

    Location getLocation(String key, String loginId);

}