package goorm.humandelivery.fare.infrastructure.kakao.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Summary {

    private int distance;
    private int duration;
    private Fare fare;

}
