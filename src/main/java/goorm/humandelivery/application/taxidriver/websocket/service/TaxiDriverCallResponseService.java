package goorm.humandelivery.application.taxidriver.websocket.service;

import goorm.humandelivery.application.CallInfoService;
import goorm.humandelivery.application.MatchingService;
import goorm.humandelivery.common.exception.AlreadyAssignedCallException;
import goorm.humandelivery.common.exception.CallAlreadyCompletedException;
import goorm.humandelivery.common.exception.IncorrectTaxiDriverStatusException;
import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.request.CallAcceptRequest;
import goorm.humandelivery.domain.model.request.CallRejectRequest;
import goorm.humandelivery.domain.model.request.CreateMatchingRequest;
import goorm.humandelivery.domain.model.response.CallAcceptResponse;
import goorm.humandelivery.domain.model.response.CallRejectResponse;
import goorm.humandelivery.application.taxidriver.TaxiDriverLoadService;
import goorm.humandelivery.infrastructure.messaging.MessagingService;
import goorm.humandelivery.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxiDriverCallResponseService {

    private final RedisService redisService;
    private final TaxiDriverLoadService taxiDriverLoadService;
    private final MatchingService matchingService;
    private final CallInfoService callInfoService;
    private final MessagingService messagingService;

    public CallAcceptResponse acceptTaxiCall(CallAcceptRequest request, String taxiDriverLoginId) {

        Long callId = request.getCallId();
        log.info("[acceptTaxiCall 호출] callId : {}, taxiDriverId : {}", callId, taxiDriverLoginId);

        TaxiDriverStatus driverStatus = redisService.getDriverStatus(taxiDriverLoginId);

        if (driverStatus != TaxiDriverStatus.AVAILABLE) {
            log.info("[acceptTaxiCall] 잘못된 상태: taxiDriver={}, callId={}, status={}", taxiDriverLoginId, callId, driverStatus);
            throw new IncorrectTaxiDriverStatusException();
        }

        if (redisService.hasAssignedCall(taxiDriverLoginId)) {
            log.info("[acceptTaxiCall] 이미 콜이 할당됨: taxiDriver={}, callId={}", taxiDriverLoginId, callId);
            throw new AlreadyAssignedCallException();
        }

        boolean isSuccess = redisService.atomicAcceptCall(callId, taxiDriverLoginId);
        if (!isSuccess) {
            log.info("[acceptTaxiCall] 완료된 콜: taxiDriver={}, callId={}", taxiDriverLoginId, callId);
            throw new CallAlreadyCompletedException();
        }

        Long taxiDriverId = taxiDriverLoadService.findIdByLoginId(taxiDriverLoginId);
        matchingService.create(new CreateMatchingRequest(callId, taxiDriverId));

        TaxiType taxiType = redisService.getDriversTaxiType(taxiDriverLoginId);
        TaxiDriverStatus updatedStatus = taxiDriverLoadService.changeStatus(taxiDriverLoginId, TaxiDriverStatus.RESERVED);
        redisService.handleTaxiDriverStatusInRedis(taxiDriverLoginId, updatedStatus, taxiType);

        CallAcceptResponse response = callInfoService.getCallAcceptResponse(callId);
        log.info("[acceptTaxiCall] 배차완료: callId={}, customerId={}, driverId={}", callId, response.getCustomerLoginId(), taxiDriverId);

        messagingService.notifyDispatchSuccessToCustomer(response.getCustomerLoginId(), taxiDriverLoginId);

        return response;
    }

    public CallRejectResponse rejectTaxiCall(CallRejectRequest request, String taxiDriverLoginId) {
        log.info("[rejectTaxiCall] 콜 거절: callId={}, taxiDriverId={}", request.getCallId(), taxiDriverLoginId);
        redisService.addRejectedDriverToCall(request.getCallId(), taxiDriverLoginId);
        return new CallRejectResponse(request.getCallId());
    }
}
