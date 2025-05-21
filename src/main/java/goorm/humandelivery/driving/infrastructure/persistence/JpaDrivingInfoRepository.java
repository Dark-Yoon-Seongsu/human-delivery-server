package goorm.humandelivery.driving.infrastructure.persistence;

import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.driving.application.port.out.LoadDrivingInfoPort;
import goorm.humandelivery.driving.application.port.out.LoadDrivingSummaryPort;
import goorm.humandelivery.driving.application.port.out.SaveDrivingInfoPort;
import goorm.humandelivery.driving.domain.DrivingInfo;
import goorm.humandelivery.driving.dto.response.DrivingSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaDrivingInfoRepository extends
        JpaRepository<DrivingInfo, Long>,
        SaveDrivingInfoPort,
        LoadDrivingInfoPort,
        LoadDrivingSummaryPort {

    Optional<DrivingInfo> findDrivingInfoByMatching(Matching matching);

    @Query("""
                select new goorm.humandelivery.driving.dto.response.DrivingSummaryResponse(
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
