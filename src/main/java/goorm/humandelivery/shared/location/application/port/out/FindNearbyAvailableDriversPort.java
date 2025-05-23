package goorm.humandelivery.shared.location.application.port.out;

import goorm.humandelivery.driver.domain.TaxiType;

import java.util.List;

public interface FindNearbyAvailableDriversPort {
    List<String> findNearByAvailableDrivers(Long callId, TaxiType taxiType, double latitude, double longitude, double radiusInKm);
}