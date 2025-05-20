package goorm.humandelivery.location.application.port.out;

import goorm.humandelivery.domain.model.entity.Location;

public interface GetLocationRedisPort {

    Location getLocation(String key, String loginId);

}