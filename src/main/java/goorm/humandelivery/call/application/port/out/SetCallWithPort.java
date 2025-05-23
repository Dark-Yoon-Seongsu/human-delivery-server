package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.call.domain.CallStatus;

public interface SetCallWithPort {

    void setCallWith(Long callId, CallStatus callStatus);

}