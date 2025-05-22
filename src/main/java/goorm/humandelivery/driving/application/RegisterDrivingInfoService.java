package goorm.humandelivery.driving.application;

import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.driving.application.port.in.RegisterDrivingInfoUseCase;
import goorm.humandelivery.driving.application.port.out.SaveDrivingInfoPort;
import goorm.humandelivery.driving.domain.DrivingInfo;
import goorm.humandelivery.driving.domain.DrivingStatus;
import goorm.humandelivery.driving.dto.request.CreateDrivingInfoRequest;
import goorm.humandelivery.global.exception.MatchingEntityNotFoundException;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterDrivingInfoService implements RegisterDrivingInfoUseCase {

    private final LoadMatchingPort loadMatchingPort;
    private final SaveDrivingInfoPort saveDrivingInfoPort;

    @Override
    public DrivingInfo create(CreateDrivingInfoRequest request) {
        Location departPosition = request.getDepartPosition();
        Long matchingId = request.getMatchingId();

        Matching matching = loadMatchingPort.findById(matchingId)
                .orElseThrow(MatchingEntityNotFoundException::new);

        LocalDateTime now = LocalDateTime.now();

        DrivingInfo drivingInfo = DrivingInfo.builder()
                .matching(matching)
                .origin(departPosition)
                .pickupTime(now)
                .drivingStatus(DrivingStatus.ON_DRIVING)
                .reported(false)
                .build();

        return saveDrivingInfoPort.save(drivingInfo);
    }
}