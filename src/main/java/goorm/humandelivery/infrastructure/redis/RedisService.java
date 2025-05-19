package goorm.humandelivery.infrastructure.redis;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import goorm.humandelivery.domain.model.entity.CallStatus;
import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.request.UpdateTaxiDriverStatusResponse;

public interface RedisService {

	// SET
	void setValueWithTTL(String key, String value, Duration ttl);
	void setValueWithTTLIfAbsent(String key, String value, Duration ttl);
	void setLocation(String key, String loginId, Location location);
	void setCallWith(Long callId, CallStatus callStatus);
	void setDriversStatus(String taxiDriverLoginId, TaxiDriverStatus status);
	void setActive(String taxiDriverLoginId);
	void setDriversTaxiType(String taxiDriverLoginId, TaxiType taxiType);
	void assignCallToDriver(Long callId, String taxiDriverLoginId);
	void addRejectedDriverToCall(Long callId, String taxiDriverLoginId);

	// GET
	String getValue(String key);
	String getLastUpdate(String reservedDriver);
	Location getLocation(String key, String loginId);
	CallStatus getCallStatus(Long callId);
	TaxiDriverStatus getDriverStatus(String driverId);
	Set<String> getActiveDrivers();
	TaxiType getDriversTaxiType(String taxiDriverLoginId);
	boolean isDriverRejectedForCall(Long callId, String taxiDriverLoginId);
	Optional<String> getCallIdByDriverId(String driverId);
	boolean hasAssignedCall(String driverId);
	Location getDriverLocation(String driverId);
	List<String> findNearByAvailableDrivers(Long callId, TaxiType type, double lat, double lon, double radiusKm);

	// DELETE
	void setOffDuty(String driverId);
	void removeFromLocation(String driverId, TaxiType type, TaxiDriverStatus status);
	void deleteCallStatus(Long callId);
	void deleteAssignedCallOf(String driverId);
	void removeRejectedDriversForCall(Long callId);
	void deleteCallBy(String driverId);
	void deleteAllLocationDataInRedis(String driverId, TaxiType taxiType);

	// STATUS
	UpdateTaxiDriverStatusResponse handleTaxiDriverStatus(String driverId, TaxiDriverStatus newStatus, TaxiType type);
}
