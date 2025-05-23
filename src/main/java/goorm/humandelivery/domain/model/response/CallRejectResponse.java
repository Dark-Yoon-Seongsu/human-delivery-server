package goorm.humandelivery.domain.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallRejectResponse {

	private Long callId;

	public CallRejectResponse(Long callId) {
		this.callId = callId;
	}
}
