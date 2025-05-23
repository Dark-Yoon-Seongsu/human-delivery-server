package goorm.humandelivery.domain.model.request;

import goorm.humandelivery.domain.model.entity.Location;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaxiCallRequest {
	private String userId;        // 사용자 ID
	private Location origin;      // 출발지 좌표
	private Location destination; // 도착지 좌표
}


