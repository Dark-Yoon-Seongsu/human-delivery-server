package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.call.domain.CallInfo;

public interface SaveCallInfoPort {

    CallInfo save(CallInfo callInfo);

}
