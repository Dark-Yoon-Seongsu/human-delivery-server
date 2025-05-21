package goorm.humandelivery.driving.application;

import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.driving.application.port.in.FinishDrivingUseCase;
import goorm.humandelivery.driving.application.port.out.LoadDrivingInfoPort;
import goorm.humandelivery.driving.application.port.out.LoadDrivingSummaryPort;
import goorm.humandelivery.driving.application.port.out.SaveDrivingInfoPort;
import goorm.humandelivery.driving.domain.DrivingInfo;
import goorm.humandelivery.driving.dto.response.DrivingSummaryResponse;
import goorm.humandelivery.global.exception.DrivingInfoEntityNotFoundException;
import goorm.humandelivery.global.exception.MatchingEntityNotFoundException;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FinishDrivingService implements FinishDrivingUseCase {

    private final LoadMatchingPort loadMatchingPort;
    private final LoadDrivingInfoPort loadDrivingInfoPort;
    private final SaveDrivingInfoPort saveDrivingInfoPort;
    private final LoadDrivingSummaryPort loadDrivingSummaryPort;

    @Override
    public DrivingSummaryResponse finishDriving(Long callId, Location destination) {
        log.info("[FinishDrivingService] 호출. callId: {}", callId);

        Matching matching = loadMatchingPort.findMatchingByCallInfoId(callId)
            .orElseThrow(MatchingEntityNotFoundException::new);

        log.info("[finishDriving.DrivingInfoService] findDrivingInfoByMatching 쿼리 호출. Call ID : {}", callId);
        DrivingInfo drivingInfo = loadDrivingInfoPort.findDrivingInfoByMatching(matching)
            .orElseThrow(DrivingInfoEntityNotFoundException::new);

        drivingInfo.finishDriving(destination, LocalDateTime.now());
        saveDrivingInfoPort.save(drivingInfo);

        log.info("[finishDriving.DrivingInfoService] findDrivingSummaryResponse 쿼리 호출. Call ID : {}", callId);
        return loadDrivingSummaryPort.findDrivingSummaryResponse(drivingInfo)
            .orElseThrow(DrivingInfoEntityNotFoundException::new);
    }
}