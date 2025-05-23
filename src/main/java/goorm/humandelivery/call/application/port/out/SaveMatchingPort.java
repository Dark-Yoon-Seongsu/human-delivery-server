package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.call.domain.Matching;

public interface SaveMatchingPort {

    Matching save(Matching matching);

}
