package goorm.humandelivery.call.infrastructure.persistence;

import goorm.humandelivery.call.application.port.out.DeleteCallInfoPort;
import goorm.humandelivery.call.application.port.out.LoadCallInfoPort;
import goorm.humandelivery.call.application.port.out.SaveCallInfoPort;
import goorm.humandelivery.call.domain.CallInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaCallInfoRepository extends
        JpaRepository<CallInfo, Long>,
        LoadCallInfoPort,
        DeleteCallInfoPort,
        SaveCallInfoPort {

    Optional<CallInfo> findCallInfoById(Long callId);

    @Query("SELECT c FROM CallInfo c join fetch c.customer cs where c.id = :callId")
    Optional<CallInfo> findCallInfoAndCustomerByCallId(Long callId);


    @Query("select cs.loginId from CallInfo c join c.customer cs where c.id = :callId")
    Optional<String> findCustomerLoginIdByCallId(Long callId);
}
