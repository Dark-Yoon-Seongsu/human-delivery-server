package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.call.domain.Matching;

public interface SaveMatchingPort {

    void save(Matching matching);

}
