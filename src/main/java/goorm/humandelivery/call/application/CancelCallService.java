package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.in.CancelCallUseCase;
import goorm.humandelivery.call.application.port.out.LoadCallInfoPort;
import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.call.application.port.out.UpdateCallInfoPort;
import goorm.humandelivery.driver.application.UpdateDriverStatusService;
import goorm.humandelivery.driver.dto.request.UpdateTaxiDriverStatusRequest;
import goorm.humandelivery.global.exception.CallInfoEntityNotFoundException;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.global.exception.CancelCallNotAllowedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CancelCallService implements CancelCallUseCase {

    private final LoadCallInfoPort loadCallInfoPort;
    private final LoadMatchingPort loadMatchingPort;
    private final UpdateCallInfoPort updateCallInfoPort;
    private final UpdateDriverStatusService updateDriverStatusService;
    private final NotifyCallCancelToDriverService notifyCallCancelToDriverService;
    private final DeleteMatchingService deleteMatchingService;

    @Override
    public void cancelCall(Long callId) {
        CallInfo call = loadCallInfoPort.findById(callId)
                .orElseThrow(CallInfoEntityNotFoundException::new);

        if (call.isCancelled()) {
            return; // 이미 취소된 콜은 무시
        }

        // 1. 매칭 여부 확인
        loadMatchingPort.findMatchingByCallInfoId(callId)
                .ifPresent(matching -> {
                    TaxiDriver driver = matching.getTaxiDriver();

                    if (driver.getStatus() == TaxiDriverStatus.ON_DRIVING) {
                        throw new CancelCallNotAllowedException("배달중인 콜은 취소할 수 없습니다.");
                    }

                    if (driver.getStatus() == TaxiDriverStatus.RESERVED) {
                        updateDriverStatusService.updateStatus(
                                new UpdateTaxiDriverStatusRequest(TaxiDriverStatus.AVAILABLE.getDescription()), driver.getLoginId());
                        notifyCallCancelToDriverService.notifyDriverOfCancelledCall(driver.getLoginId());
                    }
                    deleteMatchingService.deleteByCallId(callId);
                });

        call.cancel(); // CallInfo 내 cancel() 메서드에서 상태값을 CANCELLED로 변경
        updateCallInfoPort.cancel(call);


    }
}
