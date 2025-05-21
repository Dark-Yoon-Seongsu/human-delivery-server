package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.in.AcceptCallUseCase;
import goorm.humandelivery.call.application.port.in.GetCallAcceptResponseUseCase;
import goorm.humandelivery.call.application.port.in.RegisterMatchingUseCase;
import goorm.humandelivery.call.application.port.out.AcceptCallRedisPort;
import goorm.humandelivery.call.application.port.out.LoadTaxiDriverPort;
import goorm.humandelivery.call.dto.request.CallAcceptRequest;
import goorm.humandelivery.call.dto.request.CreateMatchingRequest;
import goorm.humandelivery.call.dto.response.CallAcceptResponse;
import goorm.humandelivery.driver.application.port.in.ChangeTaxiDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.in.UpdateDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.out.GetDriverTaxiTypeRedisPort;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.global.exception.TaxiDriverEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcceptCallService implements AcceptCallUseCase {

    private final AcceptCallRedisPort acceptCallRedisPort;
    private final LoadTaxiDriverPort loadTaxiDriverPort;
    private final RegisterMatchingUseCase registerMatchingUseCase;
    private final GetDriverTaxiTypeRedisPort getDriverTaxiTypeRedisPort;
    private final ChangeTaxiDriverStatusUseCase changeTaxiDriverStatusUseCase;
    private final UpdateDriverStatusUseCase updateDriverStatusUseCase;
    private final GetCallAcceptResponseUseCase getCallAcceptResponseUseCase;

    @Override
    public CallAcceptResponse acceptCall(CallAcceptRequest callAcceptRequest, String taxiDriverLoginId) {

        Long callId = callAcceptRequest.getCallId();
        log.info("[acceptTaxiCall 호출] callId : {}, taxiDriverId : {}", callId, taxiDriverLoginId);

        acceptCallRedisPort.atomicAcceptCall(callId, taxiDriverLoginId);

        Long taxiDriverId = loadTaxiDriverPort.findIdByLoginId(taxiDriverLoginId)
                .orElseThrow(TaxiDriverEntityNotFoundException::new);
        registerMatchingUseCase.create(new CreateMatchingRequest(callId, taxiDriverId));

        TaxiType taxiType = getDriverTaxiTypeRedisPort.getDriverTaxiType(taxiDriverLoginId);
        TaxiDriverStatus taxiDriverStatus = changeTaxiDriverStatusUseCase.changeStatus(taxiDriverLoginId, TaxiDriverStatus.RESERVED);

        // 상태 변경에 따른 redis 처리
        updateDriverStatusUseCase.updateStatus(taxiDriverLoginId, taxiDriverStatus, taxiType);

        // CallAcceptResponse 응답하기
        CallAcceptResponse callAcceptResponse = getCallAcceptResponseUseCase.getCallAcceptResponse(callId);

        log.info("[acceptTaxiCall.WebSocketTaxiDriverController] 배차완료.  콜 ID : {}, 고객 ID : {}, 택시기사 ID : {}", callId, callAcceptResponse.getCustomerLoginId(), taxiDriverId);

        // 고객에게 배차되었다고 상태 전달하기
        messagingService.notifyDispatchSuccessToCustomer(callAcceptResponse.getCustomerLoginId(), taxiDriverLoginId);

        log.info("[acceptTaxiCall 응답 보내기 전..... taxidriverId : {}]", taxiDriverLoginId);

        return callAcceptResponse;
    }
}
