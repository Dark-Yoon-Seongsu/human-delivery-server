package goorm.humandelivery.driver.application;

import goorm.humandelivery.call.application.port.out.RemoveRejectedDriversForCallPort;
import goorm.humandelivery.driver.application.port.in.DeleteAssignedCallUseCase;
import goorm.humandelivery.driver.application.port.in.HandleDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.out.*;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.response.UpdateTaxiDriverStatusResponse;
import goorm.humandelivery.global.exception.RedisKeyNotFoundException;
import goorm.humandelivery.shared.location.application.port.out.DeleteAllDriverLocationPort;
import goorm.humandelivery.shared.location.application.port.out.RemoveFromLocationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class HandleDriverStatusService implements HandleDriverStatusUseCase {

    private final SetDriverStatusPort setDriverStatusPort;
    private final SetDriverTaxiTypePort setDriverTaxiTypePort;
    private final DeleteActiveDriverPort deleteActiveDriverPort;
    private final DeleteAllDriverLocationPort deleteAllDriverLocationPort;
    private final DeleteAssignedCallUseCase deleteAssignedCallUseCase;
    private final RemoveFromLocationPort removeFromLocationPort;
    private final GetAssignedCallPort getAssignedCallPort;
    private final RemoveRejectedDriversForCallPort removeRejectedDriversForCallPort;
    private final SetActiveDriverPort setActiveDriverPort;

    @Override
    public UpdateTaxiDriverStatusResponse handleTaxiDriverStatusInRedis(String taxiDriverLoginId, TaxiDriverStatus changedStatus, TaxiType type) {

        log.info("[updateStatus : redis 택시기사 상태 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId, changedStatus);
        setDriverStatusPort.setDriverStatus(taxiDriverLoginId, changedStatus);

        log.info("[updateStatus : redis 택시기사 종류 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId, changedStatus);
        setDriverTaxiTypePort.setDriverTaxiType(taxiDriverLoginId, type);

        switch (changedStatus) {
            case OFF_DUTY -> {
                // 운행 종료. active 택시기사 목록에서 제외
                log.info("[updateStatus : 택시기사 비활성화. active 목록에서 제외] taxiDriverId : {}, 상태 : {}", taxiDriverLoginId, changedStatus);
                deleteActiveDriverPort.setOffDuty(taxiDriverLoginId);
                // 해당 기사의 위치정보 삭제
                deleteAllDriverLocationPort.deleteAllLocationData(taxiDriverLoginId, type);
                // 해당 기사가 가지고 있던 콜 삭제
                deleteAssignedCallUseCase.deleteCallBy(taxiDriverLoginId);
            }

            case AVAILABLE -> {
                deleteAssignedCallUseCase.deleteCallBy(taxiDriverLoginId);
                // 위치정보도 삭제
                removeFromLocationPort.removeFromLocation(taxiDriverLoginId, type, TaxiDriverStatus.RESERVED);
                removeFromLocationPort.removeFromLocation(taxiDriverLoginId, type, TaxiDriverStatus.ON_DRIVING);
                log.info("[updateStatus : redis 택시기사 active set 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId, changedStatus);

                // active driver set 에 없으면 추가
                setActiveDriverPort.setActiveDriver(taxiDriverLoginId);
            }

            case RESERVED -> {
                // 위치정보 삭제
                removeFromLocationPort.removeFromLocation(taxiDriverLoginId, type, TaxiDriverStatus.AVAILABLE);
                removeFromLocationPort.removeFromLocation(taxiDriverLoginId, type, TaxiDriverStatus.ON_DRIVING);

                // redis 에 저장된 콜 상태 변경  SENT -> DONE
                Optional<String> callIdOptional = getAssignedCallPort.getCallIdByDriverId(taxiDriverLoginId);

                callIdOptional.map(Long::valueOf).ifPresent(callId -> {
                    // 콜에 대한 거부 택시 기사목록 삭제
                    removeRejectedDriversForCallPort.removeRejectedDrivers(callId);
                });

                log.info("[updateStatus : redis 택시기사 active set 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId, changedStatus);

                // active driver set 에 없으면 추가
                setActiveDriverPort.setActiveDriver(taxiDriverLoginId);
            }

            case ON_DRIVING -> {
                Optional<String> callIdOptional = getAssignedCallPort.getCallIdByDriverId(taxiDriverLoginId);

                if (callIdOptional.isEmpty()) {
                    throw new RedisKeyNotFoundException("현재 기사가 가진 콜 정보가 Redis 에 존재하지 않습니다.");
                }

                callIdOptional.map(Long::valueOf).ifPresent(callId -> {
                    // 위치정보 삭제
                    removeFromLocationPort.removeFromLocation(taxiDriverLoginId, type, TaxiDriverStatus.AVAILABLE);
                    removeFromLocationPort.removeFromLocation(taxiDriverLoginId, type, TaxiDriverStatus.RESERVED);
                    // 콜에 대한 거부 택시 기사목록 삭제
                    removeRejectedDriversForCallPort.removeRejectedDrivers(callId);
                });

                log.info("[updateStatus : redis 택시기사 active set 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId, changedStatus);

                // active driver set 에 없으면 추가
                setActiveDriverPort.setActiveDriver(taxiDriverLoginId);
            }
        }

        return new UpdateTaxiDriverStatusResponse(changedStatus);
    }
}