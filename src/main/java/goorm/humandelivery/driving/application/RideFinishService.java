package goorm.humandelivery.driving.application;

import goorm.humandelivery.driver.application.port.in.ChangeTaxiDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.in.GetDriverCurrentTaxiTypeUseCase;
import goorm.humandelivery.driver.application.port.in.HandleDriverStatusUseCase;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driving.application.port.in.FinishDrivingUseCase;
import goorm.humandelivery.driving.application.port.in.RideFinishUseCase;
import goorm.humandelivery.driving.application.port.out.SendDrivingCompletedToCustomerPort;
import goorm.humandelivery.driving.application.port.out.SendDrivingCompletedToDriverPort;
import goorm.humandelivery.driving.dto.response.DrivingSummaryResponse;
import goorm.humandelivery.shared.location.application.port.in.GetDriverLocationUseCase;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RideFinishService implements RideFinishUseCase {

    private final FinishDrivingUseCase finishDrivingUseCase;
    private final ChangeTaxiDriverStatusUseCase changeTaxiDriverStatusUseCase;
    private final GetDriverCurrentTaxiTypeUseCase getDriverCurrentTaxiTypeUseCase;
    private final HandleDriverStatusUseCase handleDriverStatusUseCase;
    private final SendDrivingCompletedToCustomerPort sendToCustomerPort;
    private final SendDrivingCompletedToDriverPort sendToDriverPort;
    private final GetDriverLocationUseCase getDriverLocationUseCase;

    @Override
    public void finish(Long callId, String taxiDriverLoginId) {
        log.info("[RideFinishService.finish] 콜 종료 요청 처리 시작. callId: {}, taxiDriverLoginId: {}", callId, taxiDriverLoginId);

        Location location = getDriverLocationUseCase.getDriverLocation(taxiDriverLoginId);
        DrivingSummaryResponse response = finishDrivingUseCase.finishDriving(callId, location);

        log.info("[finishDriving.WebSocketTaxiDriverController] 택시기사 DB 상태 바꾸기 호출 전.  콜 ID : {}, 택시기사 ID : {}", callId, taxiDriverLoginId);
        TaxiDriverStatus changedStatus = changeTaxiDriverStatusUseCase.changeStatus(taxiDriverLoginId, TaxiDriverStatus.AVAILABLE);

        log.info("[finishDriving.WebSocketTaxiDriverController] 택시기사 REDIS 상태 바꾸기 호출 전.  콜 ID : {}, 택시기사 ID : {}", callId, taxiDriverLoginId);
        TaxiType taxiType = getDriverCurrentTaxiTypeUseCase.getCurrentTaxiType(taxiDriverLoginId);

        handleDriverStatusUseCase.handleTaxiDriverStatusInRedis(taxiDriverLoginId, changedStatus, taxiType);

        log.info("[finishDriving.WebSocketTaxiDriverController] 메세지 전송 전.  콜 ID : {}, 택시기사 ID : {}", callId, taxiDriverLoginId);
        sendToCustomerPort.sendToCustomer(response.getCustomerLoginId(), response);
        sendToDriverPort.sendToDriver(taxiDriverLoginId, response);

        log.info("[RideFinishService.finish] 콜 종료 처리 완료. callId: {}", callId);
    }
}