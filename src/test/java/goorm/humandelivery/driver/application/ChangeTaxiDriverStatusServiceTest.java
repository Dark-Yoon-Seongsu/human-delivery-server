package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.out.LoadTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.global.exception.DriverEntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

//./gradlew test --tests "goorm.humandelivery.driver.application.ChangeTaxiDriverStatusServiceTest"
@ExtendWith(MockitoExtension.class)
class ChangeTaxiDriverStatusServiceTest {

    @Mock
    LoadTaxiDriverPort loadTaxiDriverPort;

    @InjectMocks
    ChangeTaxiDriverStatusService changeTaxiDriverStatusService;

    TaxiDriver taxiDriver;

    @BeforeEach
    void setUp() {
        taxiDriver = TaxiDriver.builder()
                .loginId("driver1")
                .password("hashedPassword")
                .status(TaxiDriverStatus.AVAILABLE)
                .build();
    }

    @Test
    @DisplayName("상태 변경 성공")
    void changeStatus_success() {
        // given
        given(loadTaxiDriverPort.findByLoginId("driver1")).willReturn(Optional.of(taxiDriver));

        // when
        TaxiDriverStatus result = changeTaxiDriverStatusService.changeStatus("driver1", TaxiDriverStatus.ON_DRIVING);

        // then
        assertEquals(TaxiDriverStatus.ON_DRIVING, result);
    }

    @Test
    @DisplayName("존재하지 않는 loginId로 조회 시 TaxiDriverEntityNotFoundException 발생")
    void changeStatus_driverNotFound() {
        // given
        given(loadTaxiDriverPort.findByLoginId("invalidDriver")).willReturn(Optional.empty());

        // when & then
        assertThrows(DriverEntityNotFoundException.class, () -> {
            changeTaxiDriverStatusService.changeStatus("invalidDriver", TaxiDriverStatus.ON_DRIVING);
        });
    }

    @Test
    @DisplayName("변경할 상태가 null이면 IllegalArgumentException 발생")
    void changeStatus_nullStatus() {
        // given
        given(loadTaxiDriverPort.findByLoginId("driver1")).willReturn(Optional.of(taxiDriver));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            changeTaxiDriverStatusService.changeStatus("driver1", null);
        });
    }
}
