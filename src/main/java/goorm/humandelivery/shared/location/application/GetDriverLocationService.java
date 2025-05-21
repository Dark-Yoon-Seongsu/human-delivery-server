package goorm.humandelivery.shared.location.application;

import goorm.humandelivery.shared.location.domain.Location;
import goorm.humandelivery.driver.application.port.out.GetDriverStatusRedisPort;
import goorm.humandelivery.driver.application.port.out.GetDriverTaxiTypeRedisPort;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.infrastructure.redis.RedisKeyParser;
import goorm.humandelivery.shared.location.application.port.in.GetDriverLocationUseCase;
import goorm.humandelivery.shared.location.application.port.out.GetLocationRedisPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GetDriverLocationService implements GetDriverLocationUseCase {

    private final GetDriverStatusRedisPort getDriverStatusRedisPort;
    private final GetDriverTaxiTypeRedisPort getDriverTaxiTypeRedisPort;
    private final GetLocationRedisPort getLocationRedisPort;

    @Override
    public Location getDriverLocation(String driverLoginId) {
        TaxiDriverStatus taxiDriverStatus = getDriverStatusRedisPort.getDriverStatus(driverLoginId);
        TaxiType taxiType = getDriverTaxiTypeRedisPort.getDriverTaxiType(driverLoginId);
        String key = RedisKeyParser.getTaxiDriverLocationKeyBy(taxiDriverStatus, taxiType);
        return getLocationRedisPort.getLocation(key, driverLoginId);
    }
}