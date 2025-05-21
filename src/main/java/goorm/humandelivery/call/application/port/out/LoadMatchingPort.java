package goorm.humandelivery.call.application.port.out;

import goorm.humandelivery.call.domain.Matching;

import java.util.Optional;

public interface LoadMatchingPort {

    Optional<Matching> findById(Long matchingId);

    Optional<Matching> findMatchingByCallInfoId(Long callId);

    Optional<Long> findMatchingIdByCallInfoId(Long callId);

}
