package goorm.humandelivery.driver.application;

import goorm.humandelivery.call.application.port.out.RemoveRejectedDriversForCallRedisPort;
import goorm.humandelivery.driver.application.port.in.DeleteAssignedCallUseCase;
import goorm.humandelivery.driver.application.port.in.HandleDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.out.*;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.response.UpdateTaxiDriverStatusResponse;
import goorm.humandelivery.global.exception.RedisKeyNotFoundException;
import goorm.humandelivery.shared.location.application.port.out.DeleteAllDriverLocationRedisPort;
import goorm.humandelivery.shared.location.application.port.out.RemoveFromLocationRedisPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class HandleDriverStatusService implements HandleDriverStatusUseCase {

    private final SetDriverStatusRedisPort setDriverStatusRedisPort;
    private final SetDriverTaxiTypeRedisPort setDriverTaxiTypeRedisPort;
    private final DeleteActiveDriverRedisPort deleteActiveDriverRedisPort;
    private final DeleteAllDriverLocationRedisPort deleteAllDriverLocationRedisPort;
    private final DeleteAssignedCallUseCase deleteAssignedCallUseCase;
    private final RemoveFromLocationRedisPort removeFromLocationRedisPort;
    private final GetAssignedCallRedisPort getAssignedCallRedisPort;
    private final RemoveRejectedDriversForCallRedisPort removeRejectedDriversForCallRedisPort;
    private final SetActiveDriverRedisPort setActiveDriverRedisPort;

    @Override
    public UpdateTaxiDriverStatusResponse handleTaxiDriverStatusInRedis(String taxiDriverLoginId, TaxiDriverStatus changedStatus, TaxiType type) {

        log.info("[updateStatus : redis 택시기사 상태 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId, changedStatus);
        setDriverStatusRedisPort.setDriverStatus(taxiDriverLoginId, changedStatus);

        log.info("[updateStatus : redis 택시기사 종류 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId, changedStatus);
        setDriverTaxiTypeRedisPort.setDriverTaxiType(taxiDriverLoginId, type);

        switch (changedStatus) {
            case OFF_DUTY -> {
                // 운행 종료. active 택시기사 목록에서 제외
                log.info("[updateStatus : 택시기사 비활성화. active 목록에서 제외] taxiDriverId : {}, 상태 : {}", taxiDriverLoginId, changedStatus);
                deleteActiveDriverRedisPort.setOffDuty(taxiDriverLoginId);
                // 해당 기사의 위치정보 삭제
                deleteAllDriverLocationRedisPort.deleteAllLocationData(taxiDriverLoginId, type);
                // 해당 기사가 가지고 있던 콜 삭제
                deleteAssignedCallUseCase.deleteCallBy(taxiDriverLoginId);
            }

            case AVAILABLE -> {
                deleteAssignedCallUseCase.deleteCallBy(taxiDriverLoginId);
                // 위치정보도 삭제
                removeFromLocationRedisPort.removeFromLocation(taxiDriverLoginId, type, TaxiDriverStatus.RESERVED);
                removeFromLocationRedisPort.removeFromLocation(taxiDriverLoginId, type, TaxiDriverStatus.ON_DRIVING);
                log.info("[updateStatus : redis 택시기사 active set 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId, changedStatus);

                // active driver set 에 없으면 추가
                setActiveDriverRedisPort.setActiveDriver(taxiDriverLoginId);
            }

            case RESERVED -> {
                // 위치정보 삭제
                removeFromLocationRedisPort.removeFromLocation(taxiDriverLoginId, type, TaxiDriverStatus.AVAILABLE);
                removeFromLocationRedisPort.removeFromLocation(taxiDriverLoginId, type, TaxiDriverStatus.ON_DRIVING);

                // redis 에 저장된 콜 상태 변경  SENT -> DONE
                Optional<String> callIdOptional = getAssignedCallRedisPort.getCallIdByDriverId(taxiDriverLoginId);

                callIdOptional.map(Long::valueOf).ifPresent(callId -> {
                    // 콜에 대한 거부 택시 기사목록 삭제
                    removeRejectedDriversForCallRedisPort.removeRejectedDrivers(callId);
                });

                log.info("[updateStatus : redis 택시기사 active set 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId, changedStatus);

                // active driver set 에 없으면 추가
                setActiveDriverRedisPort.setActiveDriver(taxiDriverLoginId);
            }

            case ON_DRIVING -> {
                Optional<String> callIdOptional = getAssignedCallRedisPort.getCallIdByDriverId(taxiDriverLoginId);

                if (callIdOptional.isEmpty()) {
                    throw new RedisKeyNotFoundException("현재 기사가 가진 콜 정보가 Redis 에 존재하지 않습니다.");
                }

                callIdOptional.map(Long::valueOf).ifPresent(callId -> {
                    // 위치정보 삭제
                    removeFromLocationRedisPort.removeFromLocation(taxiDriverLoginId, type, TaxiDriverStatus.AVAILABLE);
                    removeFromLocationRedisPort.removeFromLocation(taxiDriverLoginId, type, TaxiDriverStatus.RESERVED);
                    // 콜에 대한 거부 택시 기사목록 삭제
                    removeRejectedDriversForCallRedisPort.removeRejectedDrivers(callId);
                });

                log.info("[updateStatus : redis 택시기사 active set 저장] taxiDriverId : {}, 상태 : {}, ", taxiDriverLoginId, changedStatus);

                // active driver set 에 없으면 추가
                setActiveDriverRedisPort.setActiveDriver(taxiDriverLoginId);
            }
        }

        return new UpdateTaxiDriverStatusResponse(changedStatus);
    }
}