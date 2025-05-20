package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.call.domain.CallStatus;

public interface SetCallWithRedisPort {

    void setCallWith(Long callId, CallStatus callStatus);

}