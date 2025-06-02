package goorm.humandelivery.fare.infrastructure.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Route {

    @JsonProperty("result_code")
    private int resultCode;

    @JsonProperty("result_msg")
    private String resultMsg;

    private Summary summary;

}
