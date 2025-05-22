package goorm.humandelivery.infrastructure.messaging;

import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.internal.CallMessage;
import goorm.humandelivery.domain.model.response.DrivingSummaryResponse;
import org.springframework.stereotype.Service;


@Service
public interface MessagingService {
    void sendLocation(String taxiDriverLoginId, TaxiDriverStatus status, TaxiType taxiType,
                      String customerLoginId, Location location);

    void sendCallMessageToTaxiDriver(String driverLoginId, CallMessage callMessage);

    void sendDrivingStartMessageToUser(String customerLoginId, boolean isDrivingStarted, boolean isDrivingFinished);
    void sendDrivingStartMessageToTaxiDriver(String taxiDriverLoginId, boolean isDrivingStarted, boolean isDrivingFinished);

    void sendDrivingCompletedMessageToUser(String customerLoginId, DrivingSummaryResponse response);
    void sendDrivingCompletedMessageToTaxiDriver(String taxiDriverLoginId, DrivingSummaryResponse response);

    void sendDispatchFailMessageToUser(String customerLoginId);
    void sendDispatchFailMessageToTaxiDriver(String driverLoginId);

    void notifyDispatchSuccessToCustomer(String customerLoginId, String driverLoginId);
    void notifyDispatchFailedToCustomer(String customerLoginId);


}
