package goorm.humandelivery.fare.application;

import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.fare.application.port.in.EstimateFareUseCase;
import goorm.humandelivery.fare.application.port.out.LoadTravelInfoPort;
import goorm.humandelivery.fare.domain.FarePolicy;
import goorm.humandelivery.fare.domain.NormalFarePolicy;
import goorm.humandelivery.fare.domain.PremiumFarePolicy;
import goorm.humandelivery.fare.domain.TravelInfo;
import goorm.humandelivery.fare.dto.request.EstimateFareRequest;
import goorm.humandelivery.fare.dto.response.EstimateFareResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstimateFareService implements EstimateFareUseCase {

    private final LoadTravelInfoPort loadTravelInfoPort;

    @Override
    public EstimateFareResponse estimateFare(EstimateFareRequest request, LocalTime requestTime) {
        TravelInfo travelInfo = loadTravelInfoPort.loadTravelInfo(request.getExpectedOrigin(), request.getExpectedDestination());
        double distance = travelInfo.getDistanceMeters();
        Map<TaxiType, BigDecimal> result = new EnumMap<>(TaxiType.class);

        for (TaxiType type : TaxiType.values()) {
            FarePolicy policy = getPolicyByType(type);
            BigDecimal fare = policy.estimateFare(distance, requestTime);
            result.put(type, fare);
            log.info("[EstimateFareService.estimateFare] 택시타입: {}, 요금: {}", type, fare);
        }

        return new EstimateFareResponse(result, travelInfo.getEstimatedFareByKakao());
    }

    private FarePolicy getPolicyByType(TaxiType type) {
        return switch (type) {
            case NORMAL -> new NormalFarePolicy();
            case PREMIUM, VENTI -> new PremiumFarePolicy();
        };
    }
}

