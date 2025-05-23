package goorm.humandelivery.driver.application;

import goorm.humandelivery.call.application.port.in.DeleteMatchingUseCase;
import goorm.humandelivery.call.application.port.out.LoadCallInfoPort;
import goorm.humandelivery.call.application.port.out.SendDispatchFailToCustomerPort;
import goorm.humandelivery.call.application.port.out.SendDispatchFailToDriverPort;
import goorm.humandelivery.driver.application.port.in.ChangeTaxiDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.in.HandleDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.out.*;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.shared.dto.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static goorm.humandelivery.driver.domain.TaxiDriverStatus.OFF_DUTY;
import static goorm.humandelivery.driver.domain.TaxiDriverStatus.RESERVED;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaxiDriverConnectionMonitor {

    private final GetActiveDriversPort getActiveDriversPort;
    private final GetDriverStatusPort getDriverStatusPort;
    private final GetDriverLastUpdatePort getDriverLastUpdatePort;
    private final GetAssignedCallPort getAssignedCallPort;
    private final GetDriverTaxiTypePort getDriverTaxiTypePort;
    private final ChangeTaxiDriverStatusUseCase changeTaxiDriverStatusUseCase;
    private final HandleDriverStatusUseCase handleDriverStatusUseCase;
    private final DeleteMatchingUseCase deleteMatchingUseCase;
    private final SendDispatchFailToDriverPort sendDispatchFailToDriverPort;
    private final SendDispatchFailToCustomerPort sendDispatchFailToCustomerPort;
    private final LoadCallInfoPort loadCallInfoPort;

    private static final long TIMEOUT_MILLIS = 10_000;

    /**
     * Scheduled 규칙
     * Method 는 void 타입으로
     * Method 는 매개변수 사용 불가
     * 이전 작업 종류 이후 시점으로부터 정의된 시간만큼 지난 후 Task를 실행한다.
     */

    // 5초에 한번씩 스케쥴링
    @Scheduled(fixedDelay = 5000)
    public void monitorReservedTaxiDrivers() {
        long now = System.currentTimeMillis();
        log.info("[monitorReservedTaxiDrivers.TaxiDriverConnectionMonitor] start monitoring at : {}", Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDateTime());

        // 1. 운행중인 기사목록 조회
        Set<String> activeDrivers = getActiveDriversPort.getActiveDrivers();

        // 2. 기사들 중 RESERVED 상태인 기사만 조회
        List<String> reservedDrivers = activeDrivers.stream()
                .filter(driverId -> getDriverStatusPort.getDriverStatus(driverId) == RESERVED)
                .toList();

        // 3. reservedDrivers 의 마지막 위치정보 시간 조회
        for (String driverLoginId : reservedDrivers) {
            String lastUpdateStr = getDriverLastUpdatePort.getLastUpdate(driverLoginId);

            if (lastUpdateStr == null || now - Long.parseLong(lastUpdateStr) > TIMEOUT_MILLIS) {
                log.warn("[{}] 위치 갱신 시간 초과.", driverLoginId);

                // 1. call Id 조회
                Optional<String> callIdOptional = getAssignedCallPort.getCallIdByDriverId(driverLoginId);

                if (callIdOptional.isEmpty()) {
                    // 해당 택시기사 상태 OFF_DUTY 로 DB 에서 변경..
                    TaxiType taxiType = getDriverTaxiTypePort.getDriverTaxiType(driverLoginId);

                    // 택시기사 상태 변경에 따른 Redis 내부 처리 로직 수행
                    TaxiDriverStatus taxiDriverStatus = changeTaxiDriverStatusUseCase.changeStatus(driverLoginId, OFF_DUTY);
                    handleDriverStatusUseCase.handleTaxiDriverStatusInRedis(driverLoginId, taxiDriverStatus, taxiType);

                    log.info("[monitorReservedTaxiDrivers.TaxiDriverConnectionMonitor] 배차 실패. 택시기사 ID : {}", driverLoginId);
                    continue;
                }

                Long callId = Long.valueOf(callIdOptional.get());
                String customerLoginId = loadCallInfoPort.findCustomerLoginIdByCallId(callId)
                        .orElseThrow(() -> new IllegalArgumentException("해당 callId의 고객 정보가 없습니다."));

                // 매칭 엔티티 삭제
                deleteMatchingUseCase.deleteByCallId(callId);

                // 3. 해당 택시기사 상태 OFF_DUTY 로 DB 에서 변경..
                TaxiType taxiType = getDriverTaxiTypePort.getDriverTaxiType(driverLoginId);

                // 4. 택시기사 상태 변경에 따른 Redis 내부 처리 로직 수행
                TaxiDriverStatus taxiDriverStatus = changeTaxiDriverStatusUseCase.changeStatus(driverLoginId, OFF_DUTY);
                handleDriverStatusUseCase.handleTaxiDriverStatusInRedis(driverLoginId, taxiDriverStatus, taxiType);

                // 5. 고객 및 택시에게 예외 메세지 전송
                log.info("[monitorReservedTaxiDrivers.TaxiDriverConnectionMonitor] 배차 실패. 예외 메세지 전송. 콜 ID : {}, 유저 ID : {}, 택시기사 ID : {}", callId, customerLoginId, driverLoginId);
                sendDispatchFailToCustomerPort.sendToCustomer(customerLoginId, new ErrorResponse("배차실패", "택시와 연결이 끊어졌습니다. 다시 배차를 시도합니다."));
                sendDispatchFailToDriverPort.sendToDriver(driverLoginId, new ErrorResponse("배차취소", "위치 미전송으로 인해 배차가 취소되었습니다."));

            }

        }

    }

}
