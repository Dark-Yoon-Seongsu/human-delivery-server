package goorm.humandelivery.fare.domain;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public class PremiumFarePolicy extends AbstractFarePolicy {
    public PremiumFarePolicy() {
        super(List.of(
                new FareCondition(LocalTime.of(22, 0), LocalTime.of(4, 0), BigDecimal.valueOf(8400), BigDecimal.valueOf(240)),
                new FareCondition(LocalTime.of(4, 0), LocalTime.of(22, 0), BigDecimal.valueOf(7000), BigDecimal.valueOf(200))
        ), 3000.0, 151.0);
    }
}
