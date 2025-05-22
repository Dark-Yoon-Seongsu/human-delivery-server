package goorm.humandelivery.infrastructure.redis;

import goorm.humandelivery.domain.model.entity.CallStatus;
import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.request.UpdateTaxiDriverStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisSetService redisSetService;
    private final RedisGetService redisGetService;
    private final RedisDeleteService redisDeleteService;
    private final RedisStatusService redisStatusService;
    private final RedisAtomicCallService redisAtomicCallService;

    // SET
    @Override
    public void setValueWithTTL(String key, String value, Duration ttl) {
        redisSetService.setValueWithTTL(key, value, ttl);
    }

    @Override
    public void setValueWithTTLIfAbsent(String key, String value, Duration ttl) {
        redisSetService.setValueWithTTLIfAbsent(key, value, ttl);
    }

    @Override
    public void setLocation(String key, String loginId, Location location) {
        redisSetService.setLocation(key, loginId, location);
    }

    @Override
    public void setCallWith(Long callId, CallStatus callStatus) {
        redisSetService.setCallWith(callId, callStatus);
    }

    @Override
    public void setDriversStatus(String taxiDriverLoginId, TaxiDriverStatus status) {
        redisSetService.setDriversStatus(taxiDriverLoginId, status);
    }

    @Override
    public void setActive(String taxiDriverLoginId) {
        redisSetService.setActive(taxiDriverLoginId);
    }

    @Override
    public void setDriversTaxiType(String taxiDriverLoginId, TaxiType taxiType) {
        redisSetService.setDriversTaxiType(taxiDriverLoginId, taxiType);
    }

    @Override
    public void assignCallToDriver(Long callId, String taxiDriverLoginId) {
        redisSetService.assignCallToDriver(callId, taxiDriverLoginId);
    }

    @Override
    public void addRejectedDriverToCall(Long callId, String taxiDriverLoginId) {
        redisSetService.addRejectedDriverToCall(callId, taxiDriverLoginId);
    }

    // GET
    @Override
    public String getValue(String key) {
        return redisGetService.getValue(key);
    }

    @Override
    public String getLastUpdate(String reservedDriver) {
        return redisGetService.getLastUpdate(reservedDriver);
    }

    @Override
    public Location getLocation(String key, String loginId) {
        return redisGetService.getLocation(key, loginId);
    }

    @Override
    public CallStatus getCallStatus(Long callId) {
        return redisGetService.getCallStatus(callId);
    }

    @Override
    public TaxiDriverStatus getDriverStatus(String driverId) {
        return redisGetService.getDriverStatus(driverId);
    }

    @Override
    public Set<String> getActiveDrivers() {
        return redisGetService.getActiveDrivers();
    }

    @Override
    public TaxiType getDriversTaxiType(String taxiDriverLoginId) {
        return redisGetService.getDriversTaxiType(taxiDriverLoginId);
    }

    @Override
    public boolean isDriverRejectedForCall(Long callId, String taxiDriverLoginId) {
        return redisGetService.isDriverRejectedForCall(callId, taxiDriverLoginId);
    }

    @Override
    public Optional<String> getCallIdByDriverId(String driverId) {
        return redisGetService.getCallIdByDriverId(driverId);
    }

    @Override
    public boolean hasAssignedCall(String driverId) {
        return redisGetService.hasAssignedCall(driverId);
    }

    @Override
    public Location getDriverLocation(String driverId) {
        return redisGetService.getDriverLocation(driverId);
    }

    @Override
    public List<String> findNearByAvailableDrivers(Long callId, TaxiType type, double lat, double lon, double radiusKm) {
        return redisGetService.findNearByAvailableDrivers(callId, type, lat, lon, radiusKm);
    }

    // DELETE
    @Override
    public void setOffDuty(String driverId) {
        redisDeleteService.setOffDuty(driverId);
    }

    @Override
    public void removeFromLocation(String driverId, TaxiType type, TaxiDriverStatus status) {
        redisDeleteService.removeFromLocation(driverId, type, status);
    }

    @Override
    public void deleteCallStatus(Long callId) {
        redisDeleteService.deleteCallStatus(callId);
    }

    @Override
    public void deleteAssignedCallOf(String driverId) {
        redisDeleteService.deleteAssignedCallOf(driverId);
    }

    @Override
    public void removeRejectedDriversForCall(Long callId) {
        redisDeleteService.removeRejectedDriversForCall(callId);
    }

    @Override
    public void deleteCallBy(String driverId) {
        redisDeleteService.deleteCallBy(driverId);
    }

    @Override
    public void deleteAllLocationDataInRedis(String driverId, TaxiType taxiType) {
        redisDeleteService.deleteAllLocationDataInRedis(driverId, taxiType);
    }

    // STATUS
    @Override
    public UpdateTaxiDriverStatusResponse handleTaxiDriverStatusInRedis(String driverId, TaxiDriverStatus newStatus, TaxiType type) {
        return redisStatusService.handleTaxiDriverStatusInRedis(driverId, newStatus, type);
    }

    @Override
    public boolean atomicAcceptCall(Long callId, String driverId) {
        return redisAtomicCallService.atomicAcceptCall(callId, driverId);
    }
}
