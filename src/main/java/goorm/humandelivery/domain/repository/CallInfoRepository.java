package goorm.humandelivery.domain.repository;

import goorm.humandelivery.domain.model.entity.CallInfo;
import goorm.humandelivery.domain.model.response.CallAcceptResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CallInfoRepository extends JpaRepository<CallInfo, Long> {

    @Query(
            "select new goorm.humandelivery.domain.model.response.CallAcceptResponse("
                    + "c.id, "
                    + "cs.name,"
                    + " cs.loginId,"
                    + " cs.phoneNumber,"
                    + " c.expectedOrigin,"
                    + " c.expectedDestination) "
                    + "from CallInfo c "
                    + "join c.customer cs "
                    + "where c.id = :callId")
    Optional<CallAcceptResponse> findCallInfoAndCustomerByCallId(Long callId);


    @Query("select cs.loginId from CallInfo c join c.customer cs where c.id = :callId")
    Optional<String> findCustomerLoginIdByCallId(long callId);
}
