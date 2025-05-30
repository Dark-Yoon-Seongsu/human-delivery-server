package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.in.GetTaxiDriverUseCase;
import goorm.humandelivery.driver.application.port.out.LoadTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.global.exception.DriverEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetTaxiDriverService implements GetTaxiDriverUseCase {

    private final LoadTaxiDriverPort loadTaxiDriverPort;

    @Override
    public TaxiDriver findById(Long id) {
        return loadTaxiDriverPort.findById(id)
                .orElseThrow(DriverEntityNotFoundException::new);
    }

    @Override
    public Long findIdByLoginId(String taxiDriverLoginId) {
        return loadTaxiDriverPort.findIdByLoginId(taxiDriverLoginId)
                .orElseThrow(DriverEntityNotFoundException::new);
    }
}
