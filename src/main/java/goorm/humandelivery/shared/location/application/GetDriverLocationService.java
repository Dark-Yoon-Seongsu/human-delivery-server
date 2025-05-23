package goorm.humandelivery.shared.location.application;

import goorm.humandelivery.driver.application.port.out.GetDriverStatusPort;
import goorm.humandelivery.driver.application.port.out.GetDriverTaxiTypePort;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import goorm.humandelivery.shared.location.application.port.in.GetDriverLocationUseCase;
import goorm.humandelivery.shared.location.application.port.out.GetLocationPort;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GetDriverLocationService implements GetDriverLocationUseCase {

    private final GetDriverStatusPort getDriverStatusPort;
    private final GetDriverTaxiTypePort getDriverTaxiTypePort;
    private final GetLocationPort getLocationPort;

    @Override
    public Location getDriverLocation(String driverLoginId) {
        TaxiDriverStatus taxiDriverStatus = getDriverStatusPort.getDriverStatus(driverLoginId);
        TaxiType taxiType = getDriverTaxiTypePort.getDriverTaxiType(driverLoginId);
        String key = RedisKeyParser.getTaxiDriverLocationKeyBy(taxiDriverStatus, taxiType);
        return getLocationPort.getLocation(key, driverLoginId);
    }
}