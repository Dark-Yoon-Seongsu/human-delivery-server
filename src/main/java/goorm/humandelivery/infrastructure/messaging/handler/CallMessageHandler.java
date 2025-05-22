package goorm.humandelivery.infrastructure.messaging.handler;

import goorm.humandelivery.application.CallInfoService;
import goorm.humandelivery.common.exception.NoAvailableTaxiException;
import goorm.humandelivery.domain.model.internal.CallMessage;
import goorm.humandelivery.infrastructure.messaging.MessagingService;
import goorm.humandelivery.service.CallRequestDispatchService;
import goorm.humandelivery.service.NearTaxiSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallMessageHandler {

    private final NearTaxiSearchService nearTaxiSearchService;
    private final CallInfoService callInfoService;
    private final MessagingService messagingService;
    private final CallRequestDispatchService callRequestDispatchService;

    public void handle(CallMessage callMessage) {
        List<String> availableDrivers = nearTaxiSearchService.findNearByAvailableDrivers(callMessage);

        log.info("범위 내 유효한 택시 수: {}", availableDrivers.size());

        if (availableDrivers.isEmpty()) {
            log.info("유효한 택시가 없음");
            callInfoService.deleteCallById(callMessage.getCallId());
            messagingService.notifyDispatchFailedToCustomer(callMessage.getCustomerLoginId());
            throw new NoAvailableTaxiException();
        }

        callRequestDispatchService.dispatchCallRequest(callMessage, availableDrivers);
    }
}
