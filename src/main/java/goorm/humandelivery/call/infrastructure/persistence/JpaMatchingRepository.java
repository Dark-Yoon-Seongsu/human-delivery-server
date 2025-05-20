package goorm.humandelivery.call.infrastructure.persistence;

import goorm.humandelivery.call.application.port.out.DeleteMatchingPort;
import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.call.application.port.out.SaveMatchingPort;
import goorm.humandelivery.call.domain.Matching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaMatchingRepository extends
        JpaRepository<Matching, Long>,
        SaveMatchingPort,
        LoadMatchingPort,
        DeleteMatchingPort {

    @Query("select m from Matching m where m.callInfo.id = :callId")
    Optional<Matching> findMatchingByCallInfoId(Long callId);

    @Query("select m.id from Matching m where m.callInfo.id = :callId")
    Optional<Long> findMatchingIdByCallInfoId(Long callId);
}
