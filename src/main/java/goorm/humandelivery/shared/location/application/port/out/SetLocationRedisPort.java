package goorm.humandelivery.shared.location.application.port.out;

import goorm.humandelivery.shared.location.domain.Location;

public interface SetLocationRedisPort {
    void setLocation(String key, String loginId, Location location);
}