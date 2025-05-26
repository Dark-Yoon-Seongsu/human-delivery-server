package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.in.RegisterMatchingUseCase;
import goorm.humandelivery.call.application.port.out.LoadCallInfoPort;
import goorm.humandelivery.call.application.port.out.SaveMatchingPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.call.dto.request.CreateMatchingRequest;
import goorm.humandelivery.driver.application.port.in.GetTaxiDriverUseCase;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.global.exception.CallInfoEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterMatchingService implements RegisterMatchingUseCase {

    private final LoadCallInfoPort loadCallInfoPort;
    private final GetTaxiDriverUseCase getTaxiDriverUseCase;
    private final SaveMatchingPort saveMatchingPort;

    @Override
    public void create(CreateMatchingRequest createMatchingRequest) {
        CallInfo call = loadCallInfoPort.findById(createMatchingRequest.getCallId())
                .orElseThrow(CallInfoEntityNotFoundException::new);
        TaxiDriver driver = getTaxiDriverUseCase.findById(createMatchingRequest.getTaxiDriverId());

        Matching matching = Matching.builder()
                .callInfo(call)
                .taxiDriver(driver)
                .build();

        saveMatchingPort.save(matching);
    }
}
