package goorm.humandelivery.call.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CallRejectResponse {

    private Long callId;

    public CallRejectResponse(Long callId) {
        this.callId = callId;
    }
}
