package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.in.GetDriverCurrentStatusUseCase;
import goorm.humandelivery.driver.application.port.in.GetDriverCurrentTaxiTypeUseCase;
import goorm.humandelivery.driver.application.port.in.SendDriverLocationToCustomerUseCase;
import goorm.humandelivery.driver.application.port.in.UpdateDriverLocationUseCase;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.request.UpdateDriverLocationRequest;
import goorm.humandelivery.global.exception.OffDutyLocationUpdateException;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UpdateDriverLocationService implements UpdateDriverLocationUseCase {

    private final GetDriverCurrentStatusUseCase getDriverCurrentStatusUseCase;
    private final GetDriverCurrentTaxiTypeUseCase getDriverCurrentTaxiTypeUseCase;
    private final SendDriverLocationToCustomerUseCase sendDriverLocationToCustomerUseCase;

    @Override
    public void updateLocation(UpdateDriverLocationRequest request, String taxiDriverLoginId) {
        Location location = request.getLocation();
        String customerId = request.getCustomerLoginId();
        log.info("[updateLocation 호출] taxiDriverId : {}, 위도 : {}, 경도 : {}", taxiDriverLoginId, location.getLatitude(), location.getLongitude());

        TaxiDriverStatus status = getDriverCurrentStatusUseCase.getCurrentStatus(taxiDriverLoginId);
        TaxiType taxiType = getDriverCurrentTaxiTypeUseCase.getCurrentTaxiType(taxiDriverLoginId);

        if (status == TaxiDriverStatus.OFF_DUTY) {
            throw new OffDutyLocationUpdateException();
        }

        log.info("[UpdateDriverLocationService] 위치 갱신 요청 - driverId: {}, 상태: {}, 타입: {}, 고객 ID: {}", taxiDriverLoginId, status, taxiType, customerId);

        sendDriverLocationToCustomerUseCase.sendLocation(taxiDriverLoginId, status, taxiType, customerId, location);
    }
}