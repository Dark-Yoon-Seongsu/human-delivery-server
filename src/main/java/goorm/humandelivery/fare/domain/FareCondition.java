package goorm.humandelivery.fare.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class FareCondition {

    private final LocalTime start;
    private final LocalTime end;
    private final BigDecimal baseFare;
    private final BigDecimal unitFare;

    public boolean isMatch(LocalTime time) {
        return start.isBefore(end)
                ? !time.isBefore(start) && time.isBefore(end)
                : !time.isBefore(start) || time.isBefore(end);
    }


}
