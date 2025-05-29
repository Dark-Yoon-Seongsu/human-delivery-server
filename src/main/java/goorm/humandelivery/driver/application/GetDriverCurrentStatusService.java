package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.in.GetDriverCurrentStatusUseCase;
import goorm.humandelivery.driver.application.port.out.LoadTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.global.exception.DriverEntityNotFoundException;
import goorm.humandelivery.shared.application.port.out.GetValuePort;
import goorm.humandelivery.shared.application.port.out.SetValueWithTtlPort;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class GetDriverCurrentStatusService implements GetDriverCurrentStatusUseCase {

    private final GetValuePort getValuePort;
    private final SetValueWithTtlPort setValueWithTtlPort;
    private final LoadTaxiDriverPort loadTaxiDriverPort;

    @Override
    public TaxiDriverStatus getCurrentStatus(String driverLoginId) {
        String key = RedisKeyParser.taxiDriverStatus(driverLoginId);

        String status = getValuePort.getValue(key);

        if (status != null) {
            return TaxiDriverStatus.valueOf(status);
        }

        TaxiDriverStatus dbStatus = loadTaxiDriverPort.findByLoginId(driverLoginId)
                .orElseThrow(DriverEntityNotFoundException::new)
                .getStatus();

        setValueWithTtlPort.setValueWithTTL(key, dbStatus.name(), Duration.ofHours(1));

        return dbStatus;
    }
}