package goorm.humandelivery.fare.domain;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
public abstract class AbstractFarePolicy implements FarePolicy {

    private final List<FareCondition> fareConditions;
    private final double baseDistance;
    private final double unitDistance;

    @Override
    public BigDecimal estimateFare(double distance, LocalTime requestTime) {
        FareCondition matched = fareConditions.stream()
                .filter(c -> c.isMatch(requestTime))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("적용 가능한 요금이 없습니다."));

        BigDecimal fare = matched.getBaseFare();
        if (distance > baseDistance) {
            double extra = distance - baseDistance;
            BigDecimal unitCount = BigDecimal.valueOf(extra / unitDistance).setScale(0, RoundingMode.DOWN);
            fare = fare.add(matched.getUnitFare().multiply(unitCount));
        }

        return fare.setScale(0, RoundingMode.HALF_UP);
    }
}
