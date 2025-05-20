package goorm.humandelivery.location.infrastructure.redis;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.location.application.port.out.DeleteAllDriverLocationRedisPort;
import goorm.humandelivery.location.application.port.out.RemoveFromLocationRedisPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DeleteAllDriverLocationRedisAdapter implements DeleteAllDriverLocationRedisPort {

    private final RemoveFromLocationRedisPort removeFromLocationRedisPort;

    @Override
    public void deleteAllLocationData(String driverLoginId, TaxiType taxiType) {
        removeFromLocationRedisPort.removeFromLocation(driverLoginId, taxiType, TaxiDriverStatus.AVAILABLE);
        removeFromLocationRedisPort.removeFromLocation(driverLoginId, taxiType, TaxiDriverStatus.RESERVED);
        removeFromLocationRedisPort.removeFromLocation(driverLoginId, taxiType, TaxiDriverStatus.ON_DRIVING);
    }
}