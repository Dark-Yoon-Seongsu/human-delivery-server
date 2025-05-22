package goorm.humandelivery.driving.application;

import goorm.humandelivery.call.application.port.out.LoadCallInfoPort;
import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.driver.application.port.in.ChangeTaxiDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.in.GetDriverCurrentTaxiTypeUseCase;
import goorm.humandelivery.driver.application.port.in.HandleDriverStatusUseCase;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driving.application.port.in.RegisterDrivingInfoUseCase;
import goorm.humandelivery.driving.application.port.in.RideStartUseCase;
import goorm.humandelivery.driving.application.port.out.SendDrivingStartToCustomerPort;
import goorm.humandelivery.driving.application.port.out.SendDrivingStartToDriverPort;
import goorm.humandelivery.driving.domain.DrivingInfo;
import goorm.humandelivery.driving.dto.request.CreateDrivingInfoRequest;
import goorm.humandelivery.driving.dto.response.DrivingInfoResponse;
import goorm.humandelivery.shared.location.application.port.in.GetDriverLocationUseCase;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class RideStartService implements RideStartUseCase {

    private final LoadMatchingPort loadMatchingPort;
    private final GetDriverLocationUseCase getDriverLocationUseCase;
    private final RegisterDrivingInfoUseCase registerDrivingInfoUseCase;
    private final ChangeTaxiDriverStatusUseCase changeTaxiDriverStatusUseCase;
    private final GetDriverCurrentTaxiTypeUseCase getDriverCurrentTaxiTypeUseCase;
    private final HandleDriverStatusUseCase handleDriverStatusUseCase;
    private final LoadCallInfoPort loadCallInfoPort;
    private final SendDrivingStartToCustomerPort sendDrivingStartToCustomerPort;
    private final SendDrivingStartToDriverPort sendDrivingStartToDriverPort;

    @Override
    public void rideStart(Long callId, String taxiDriverLoginId) {
        log.info("[StartDrivingService.start] 고객 승차 요청 처리 시작. callId: {}, taxiDriverLoginId: {}", callId, taxiDriverLoginId);

        Long matchingId = loadMatchingPort.findMatchingIdByCallInfoId(callId)
                .orElseThrow(() -> new IllegalArgumentException("해당 callId에 매칭이 없습니다."));

        Location location = getDriverLocationUseCase.getDriverLocation(taxiDriverLoginId);

        DrivingInfo drivingInfo = registerDrivingInfoUseCase.create(new CreateDrivingInfoRequest(matchingId, location));

        TaxiDriverStatus updatedStatus = changeTaxiDriverStatusUseCase.changeStatus(taxiDriverLoginId, TaxiDriverStatus.ON_DRIVING);
        TaxiType taxiType = getDriverCurrentTaxiTypeUseCase.getCurrentTaxiType(taxiDriverLoginId);
        handleDriverStatusUseCase.handleTaxiDriverStatusInRedis(taxiDriverLoginId, updatedStatus, taxiType);

        String customerLoginId = loadCallInfoPort.findCustomerLoginIdByCallId(callId)
                .orElseThrow(() -> new IllegalArgumentException("해당 callId의 고객 정보가 없습니다."));

        boolean isDrivingStarted = drivingInfo.isDrivingStarted();

        sendDrivingStartToCustomerPort.sendToCustomer(customerLoginId, new DrivingInfoResponse(isDrivingStarted, false));
        sendDrivingStartToDriverPort.sendToDriver(taxiDriverLoginId, new DrivingInfoResponse(isDrivingStarted, false));

        log.info("[StartDrivingService.start] 승차 프로세스 완료. callId: {}", callId);
    }
}