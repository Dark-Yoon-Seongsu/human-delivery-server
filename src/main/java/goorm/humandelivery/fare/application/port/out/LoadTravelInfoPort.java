package goorm.humandelivery.fare.application.port.out;

import goorm.humandelivery.fare.domain.TravelInfo;
import goorm.humandelivery.shared.location.domain.Location;

public interface LoadTravelInfoPort {
    TravelInfo loadTravelInfo(Location origin, Location destination);
}