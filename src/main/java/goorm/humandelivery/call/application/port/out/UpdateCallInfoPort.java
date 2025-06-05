package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.call.domain.CallInfo;

public interface UpdateCallInfoPort {
    void cancel(CallInfo callInfo);
}
