package goorm.humandelivery.domain.model.response;

import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.internal.CallMessage;

public class CallMessageResponse {
    private Long callId;
    private Location expectedOrigin;
    private Location expectedDestination;

    public void from(CallMessage message) {
        this.callId = message.getCallId();
        this.expectedOrigin = message.getExpectedOrigin();
        this.expectedDestination = message.getExpectedDestination();
    }
}
