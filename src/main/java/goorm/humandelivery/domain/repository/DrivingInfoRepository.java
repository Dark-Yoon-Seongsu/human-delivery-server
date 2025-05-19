package goorm.humandelivery.domain.repository;

import goorm.humandelivery.domain.model.entity.DrivingInfo;
import goorm.humandelivery.domain.model.entity.Matching;
import goorm.humandelivery.domain.model.response.DrivingSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DrivingInfoRepository extends JpaRepository<DrivingInfo, Long> {
    Optional<DrivingInfo> findDrivingInfoByMatching(Matching matching);

    @Query("""
                select new goorm.humandelivery.domain.model.response.DrivingSummaryResponse(
                    ci.id, 
                    cs.loginId, 
                    td.loginId, 
                    d.origin, 
                    d.pickupTime, 
                    d.destination, 
                    d.arrivingTime, 
                    d.drivingStatus, 
                    d.reported
                )
                from DrivingInfo d
                join d.matching m
                join m.callInfo ci
                join m.taxiDriver td
                join ci.customer cs
                where d = :drivingInfo
            """)
    Optional<DrivingSummaryResponse> findDrivingSummaryResponse(@Param("drivingInfo") DrivingInfo drivingInfo);
}
