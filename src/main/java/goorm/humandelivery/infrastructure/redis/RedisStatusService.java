package goorm.humandelivery.infrastructure.redis;

import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.request.UpdateTaxiDriverStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisStatusService {

    private final RedisSetService redisSetService;
    private final RedisDeleteService redisDeleteService;
    private final RedisGetService redisGetService;

    public UpdateTaxiDriverStatusResponse handleTaxiDriverStatusInRedis(String driverId, TaxiDriverStatus newStatus, TaxiType type) {

        redisSetService.setDriversStatus(driverId, newStatus);
        redisSetService.setDriversTaxiType(driverId, type);

        switch (newStatus) {
            case OFF_DUTY -> {
                redisDeleteService.setOffDuty(driverId);
                redisDeleteService.deleteAllLocationDataInRedis(driverId, type);
                redisDeleteService.deleteCallBy(driverId);
            }
            case AVAILABLE -> {
                redisDeleteService.deleteCallBy(driverId);
                redisDeleteService.removeFromLocation(driverId, type, TaxiDriverStatus.RESERVED);
                redisDeleteService.removeFromLocation(driverId, type, TaxiDriverStatus.ON_DRIVING);
                redisSetService.setActive(driverId);
            }
            case RESERVED, ON_DRIVING -> {
                redisDeleteService.removeFromLocation(driverId, type, TaxiDriverStatus.AVAILABLE);
                redisDeleteService.removeFromLocation(driverId, type, newStatus == TaxiDriverStatus.RESERVED ? TaxiDriverStatus.ON_DRIVING : TaxiDriverStatus.RESERVED);

                redisGetService.getCallIdByDriverId(driverId).ifPresent(callIdStr -> {
                    Long callId = Long.valueOf(callIdStr);
                    redisDeleteService.removeRejectedDriversForCall(callId);
                });

                redisSetService.setActive(driverId);
            }
        }

        return new UpdateTaxiDriverStatusResponse(newStatus);
    }
}
