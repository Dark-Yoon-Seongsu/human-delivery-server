package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.in.ChangeTaxiDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.out.LoadTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.global.exception.TaxiDriverEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChangeTaxiDriverStatusService implements ChangeTaxiDriverStatusUseCase {

    private final LoadTaxiDriverPort loadTaxiDriverPort;

    @Override
    public TaxiDriverStatus changeStatus(String loginId, TaxiDriverStatus status) {
        log.info("[ChangeTaxiDriverStatusService.changeStatus.loadTaxiDriverPort.findByLoginId] 택시기사 조회. 택시기사 ID : {}", loginId);
        TaxiDriver taxiDriver = loadTaxiDriverPort.findByLoginId(loginId)
                .orElseThrow(TaxiDriverEntityNotFoundException::new);

        return taxiDriver.changeStatus(status);
    }
}
