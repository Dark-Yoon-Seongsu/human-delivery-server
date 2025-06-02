package goorm.humandelivery.fare.domain;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public class NormalFarePolicy extends AbstractFarePolicy {
    public NormalFarePolicy() {
        super(List.of(
                new FareCondition(LocalTime.of(23, 0), LocalTime.of(2, 0), BigDecimal.valueOf(6700), BigDecimal.valueOf(140)),
                new FareCondition(LocalTime.of(22, 0), LocalTime.of(4, 0), BigDecimal.valueOf(5800), BigDecimal.valueOf(120)),
                new FareCondition(LocalTime.of(4, 0), LocalTime.of(22, 0), BigDecimal.valueOf(4800), BigDecimal.valueOf(100))
        ), 1600.0, 131.0);
    }
}
