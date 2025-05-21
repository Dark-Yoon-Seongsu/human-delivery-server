package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.in.ChangeTaxiDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.in.GetDriverCurrentTaxiTypeUseCase;
import goorm.humandelivery.driver.application.port.in.HandleDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.in.UpdateDriverStatusUseCase;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.request.UpdateTaxiDriverStatusRequest;
import goorm.humandelivery.driver.dto.response.UpdateTaxiDriverStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UpdateDriverStatusService implements UpdateDriverStatusUseCase {

    private final ChangeTaxiDriverStatusUseCase changeTaxiDriverStatusUseCase;
    private final HandleDriverStatusUseCase handleDriverStatusUseCase;
    private final GetDriverCurrentTaxiTypeUseCase getDriverCurrentTaxiTypeUseCase;

    @Override
    public UpdateTaxiDriverStatusResponse updateStatus(UpdateTaxiDriverStatusRequest request, String driverLoginId) {
        TaxiDriverStatus statusToBe = TaxiDriverStatus.valueOf(request.getStatus());

        log.info("[UpdateDriverStatusService] 택시기사 DB 상태 변경 호출. driverId: {}, 변경될 상태: {}", driverLoginId, statusToBe);
        TaxiDriverStatus changedStatus = changeTaxiDriverStatusUseCase.changeStatus(driverLoginId, statusToBe);

        log.info("[UpdateDriverStatusService] 택시기사 타입 조회. driverId: {}", driverLoginId);
        TaxiType taxiType = getDriverCurrentTaxiTypeUseCase.getCurrentTaxiType(driverLoginId);

        log.info("[UpdateDriverStatusService] Redis에 상태 반영 시작. driverId: {}, 상태: {}, taxiType: {}", driverLoginId, changedStatus, taxiType);
        return handleDriverStatusUseCase.handleTaxiDriverStatusInRedis(driverLoginId, changedStatus, taxiType);
    }
}