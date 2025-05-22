package goorm.humandelivery.infrastructure.messaging;

import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.internal.CallMessage;
import goorm.humandelivery.domain.model.response.DrivingSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MassagingServiceImpl implements MessagingService {

    private final CallMessagingService callMessagingService;
    private final DispatchMessagingService dispatchMessagingService;
    private final LocationMessagingService locationMessagingService;
    private final DrivingStatusMessagingService drivingStatusMessagingService;

    @Override
    public void sendLocation(String taxiDriverLoginId, TaxiDriverStatus status, TaxiType taxiType,
                             String customerLoginId, Location location) {
        locationMessagingService.sendLocation(taxiDriverLoginId, status, taxiType, customerLoginId, location);
    }

    @Override
    public void sendCallMessageToTaxiDriver(String driverLoginId, CallMessage callMessage) {
        callMessagingService.sendCallMessageToTaxiDriver(driverLoginId, callMessage);
    }

    @Override
    public void sendDrivingStartMessageToUser(String customerLoginId, boolean isDrivingStarted, boolean isDrivingFinished) {
        drivingStatusMessagingService.sendDrivingStartMessageToUser(customerLoginId, isDrivingStarted, isDrivingFinished);
    }

    @Override
    public void sendDrivingStartMessageToTaxiDriver(String taxiDriverLoginId, boolean isDrivingStarted, boolean isDrivingFinished) {
        drivingStatusMessagingService.sendDrivingStartMessageToTaxiDriver(taxiDriverLoginId, isDrivingStarted, isDrivingFinished);
    }

    @Override
    public void sendDrivingCompletedMessageToUser(String customerLoginId, DrivingSummaryResponse response) {
        drivingStatusMessagingService.sendDrivingCompletedMessageToUser(customerLoginId, response);
    }

    @Override
    public void sendDrivingCompletedMessageToTaxiDriver(String taxiDriverLoginId, DrivingSummaryResponse response) {
        drivingStatusMessagingService.sendDrivingCompletedMessageToTaxiDriver(taxiDriverLoginId, response);
    }

    @Override
    public void sendDispatchFailMessageToUser(String customerLoginId) {
        dispatchMessagingService.sendDispatchFailMessageToUser(customerLoginId);
    }

    @Override
    public void sendDispatchFailMessageToTaxiDriver(String driverLoginId) {
        dispatchMessagingService.sendDispatchFailMessageToTaxiDriver(driverLoginId);
    }

    @Override
    public void notifyDispatchSuccessToCustomer(String customerLoginId, String driverLoginId) {
        dispatchMessagingService.notifyDispatchSuccessToCustomer(customerLoginId, driverLoginId);
    }

    @Override
    public void notifyDispatchFailedToCustomer(String customerLoginId) {
        dispatchMessagingService.notifyDispatchFailedToCustomer(customerLoginId);
    }
}
