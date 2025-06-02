package goorm.humandelivery.fare.domain;

import java.math.BigDecimal;
import java.time.LocalTime;

public interface FarePolicy {

    BigDecimal estimateFare(double distance, LocalTime requestTime);

}
