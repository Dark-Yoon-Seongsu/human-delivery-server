package goorm.humandelivery.location.application.port.out;

import goorm.humandelivery.domain.model.entity.Location;

public interface SetLocationRedisPort {
    void setLocation(String key, String loginId, Location location);
}