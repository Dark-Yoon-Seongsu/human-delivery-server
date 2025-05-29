package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.in.GetDriverCurrentTaxiTypeUseCase;
import goorm.humandelivery.driver.application.port.out.LoadTaxiDriverTypePort;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.response.TaxiTypeResponse;
import goorm.humandelivery.global.exception.DriverEntityNotFoundException;
import goorm.humandelivery.shared.application.port.out.GetValuePort;
import goorm.humandelivery.shared.application.port.out.SetValueWithTtlPort;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class GetDriverCurrentTaxiTypeService implements GetDriverCurrentTaxiTypeUseCase {

    private final GetValuePort getValuePort;
    private final SetValueWithTtlPort setValueWithTtlPort;
    private final LoadTaxiDriverTypePort loadTaxiDriverTypePort;

    @Override
    public TaxiType getCurrentTaxiType(String driverLoginId) {
        String key = RedisKeyParser.taxiDriversTaxiType(driverLoginId);
        String cached = getValuePort.getValue(key);

        if (cached != null) {
            return TaxiType.valueOf(cached);
        }

        TaxiTypeResponse taxiTypeResponse = loadTaxiDriverTypePort.findTaxiDriversTaxiTypeByLoginId(driverLoginId)
                .orElseThrow(DriverEntityNotFoundException::new);
        TaxiType taxiType = taxiTypeResponse.getTaxiType();

        setValueWithTtlPort.setValueWithTTL(key, taxiType.name(), Duration.ofDays(1));

        return taxiType;
    }
}