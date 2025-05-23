package goorm.humandelivery.shared.location.infrastructure.redis;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.shared.location.application.port.out.DeleteAllDriverLocationPort;
import goorm.humandelivery.shared.location.application.port.out.RemoveFromLocationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DeleteAllDriverLocationRedisAdapter implements DeleteAllDriverLocationPort {

    private final RemoveFromLocationPort removeFromLocationPort;

    @Override
    public void deleteAllLocationData(String driverLoginId, TaxiType taxiType) {
        removeFromLocationPort.removeFromLocation(driverLoginId, taxiType, TaxiDriverStatus.AVAILABLE);
        removeFromLocationPort.removeFromLocation(driverLoginId, taxiType, TaxiDriverStatus.RESERVED);
        removeFromLocationPort.removeFromLocation(driverLoginId, taxiType, TaxiDriverStatus.ON_DRIVING);
    }
}