package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.in.RegisterTaxiDriverUseCase;
import goorm.humandelivery.driver.application.port.out.SaveTaxiDriverPort;
import goorm.humandelivery.driver.application.port.out.SaveTaxiPort;
import goorm.humandelivery.driver.domain.*;
import goorm.humandelivery.driver.dto.request.RegisterTaxiDriverRequest;
import goorm.humandelivery.driver.dto.request.RegisterTaxiRequest;
import goorm.humandelivery.driver.dto.response.RegisterTaxiDriverResponse;
import goorm.humandelivery.global.exception.DuplicateLoginIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterTaxiDriverService implements RegisterTaxiDriverUseCase {

    private final SaveTaxiDriverPort saveTaxiDriverPort;
    private final SaveTaxiPort saveTaxiPort;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public RegisterTaxiDriverResponse register(RegisterTaxiDriverRequest request) {

        if (saveTaxiDriverPort.existsByLoginId(request.getLoginId())) {
            throw new DuplicateLoginIdException();
        }

        RegisterTaxiRequest taxiRequest = request.getTaxi();

        Taxi taxi = Taxi.builder()
                .model(taxiRequest.getModel())
                .plateNumber(taxiRequest.getPlateNumber())
                .taxiType(TaxiType.valueOf(taxiRequest.getTaxiType()))
                .fuelType(FuelType.valueOf(taxiRequest.getFuelType()))
                .build();

        Taxi savedTaxi = saveTaxiPort.save(taxi);

        TaxiDriver driver = TaxiDriver.builder()
                .taxi(savedTaxi)
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .licenseCode(request.getLicenseCode())
                .phoneNumber(request.getPhoneNumber())
                .status(TaxiDriverStatus.OFF_DUTY)
                .build();

        TaxiDriver savedDriver = saveTaxiDriverPort.save(driver);

        return RegisterTaxiDriverResponse.from(savedDriver);
    }
}
