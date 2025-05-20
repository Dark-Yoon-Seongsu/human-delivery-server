package goorm.humandelivery.application;

import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.domain.model.entity.DrivingInfo;
import goorm.humandelivery.domain.model.entity.DrivingStatus;
import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.request.CreateDrivingInfoRequest;
import goorm.humandelivery.domain.model.response.DrivingSummaryResponse;
import goorm.humandelivery.domain.repository.DrivingInfoRepository;
import goorm.humandelivery.domain.repository.MatchingRepository;
import goorm.humandelivery.global.exception.DrivingInfoEntityNotFoundException;
import goorm.humandelivery.global.exception.MatchingEntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional(readOnly = true)
public class DrivingInfoService {

    private final DrivingInfoRepository drivingInfoRepository;
    private final MatchingRepository matchingRepository;

    @Autowired
    public DrivingInfoService(DrivingInfoRepository drivingInfoRepository, MatchingRepository matchingRepository) {
        this.drivingInfoRepository = drivingInfoRepository;
        this.matchingRepository = matchingRepository;
    }

    @Transactional
    public DrivingInfo create(CreateDrivingInfoRequest request) {

        Location departPosition = request.getDepartPosition();

        Long matchingId = request.getMatchingId();
        Matching matching = matchingRepository.findById(matchingId).orElseThrow(MatchingEntityNotFoundException::new);

        LocalDateTime now = LocalDateTime.now();

        DrivingInfo drivingInfo = DrivingInfo.builder()
                .matching(matching)
                .origin(departPosition)
                .pickupTime(now)
                .drivingStatus(DrivingStatus.ON_DRIVING)
                .reported(false)
                .build();

        return drivingInfoRepository.save(drivingInfo);
    }

    @Transactional
    public DrivingSummaryResponse finishDriving(Long callId, Location destination) {
        log.info("[finishDriving.DrivingInfoService] 호출. Call ID : {}", callId);
        Matching matching = matchingRepository.findMatchingByCallInfoId(callId)
                .orElseThrow(MatchingEntityNotFoundException::new);

        log.info("[finishDriving.DrivingInfoService] findDrivingInfoByMatching 쿼리 호출. Call ID : {}", callId);
        DrivingInfo drivingInfo = drivingInfoRepository.findDrivingInfoByMatching(matching)
                .orElseThrow(DrivingInfoEntityNotFoundException::new);

        // 운행 종료
        LocalDateTime arrivingTime = LocalDateTime.now();
        drivingInfo.finishDriving(destination, arrivingTime);

        // 필요한 데이터만 조회해서 DTO 로 반환하자.
        log.info("[finishDriving.DrivingInfoService] findDrivingSummaryResponse 쿼리 호출. Call ID : {}", callId);
        return drivingInfoRepository.findDrivingSummaryResponse(drivingInfo)
                .orElseThrow(DrivingInfoEntityNotFoundException::new);

    }

}
