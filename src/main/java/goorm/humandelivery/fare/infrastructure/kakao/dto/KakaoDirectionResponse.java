package goorm.humandelivery.fare.infrastructure.kakao.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KakaoDirectionResponse {

    private List<Route> routes;

}