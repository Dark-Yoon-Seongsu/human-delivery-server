package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.in.GetDriverCurrentStatusUseCase;
import goorm.humandelivery.driver.application.port.in.GetDriverCurrentTaxiTypeUseCase;
import goorm.humandelivery.driver.application.port.in.SendDriverLocationToCustomerUseCase;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.request.UpdateDriverLocationRequest;
import goorm.humandelivery.global.exception.OffDutyLocationUpdateException;
import goorm.humandelivery.shared.location.domain.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//./gradlew test --tests "goorm.humandelivery.driver.application.UpdateDriverLocationServiceTest"
@ExtendWith(MockitoExtension.class)
class UpdateDriverLocationServiceTest {

    private GetDriverCurrentStatusUseCase getStatusUseCase;
    private GetDriverCurrentTaxiTypeUseCase getTaxiTypeUseCase;
    private SendDriverLocationToCustomerUseCase sendLocationUseCase;
    private UpdateDriverLocationService service;

    @BeforeEach
    void setUp() {
        getStatusUseCase = mock(GetDriverCurrentStatusUseCase.class);
        getTaxiTypeUseCase = mock(GetDriverCurrentTaxiTypeUseCase.class);
        sendLocationUseCase = mock(SendDriverLocationToCustomerUseCase.class);

        service = new UpdateDriverLocationService(getStatusUseCase, getTaxiTypeUseCase, sendLocationUseCase);
    }

    @Test
    void 위치_정상_갱신() {
        // given
        String driverId = "driver123";
        String customerId = "customer456";
        Location location = new Location(37.5665, 126.9780);
        UpdateDriverLocationRequest request = new UpdateDriverLocationRequest(customerId, location);

        when(getStatusUseCase.getCurrentStatus(driverId)).thenReturn(TaxiDriverStatus.RESERVED);
        when(getTaxiTypeUseCase.getCurrentTaxiType(driverId)).thenReturn(TaxiType.NORMAL);

        // when
        service.updateLocation(request, driverId);

        // then
        verify(sendLocationUseCase).sendLocation(eq(driverId), eq(TaxiDriverStatus.RESERVED), eq(TaxiType.NORMAL), eq(customerId), eq(location));
    }

    @Test
    void OFF_DUTY_상태일때_예외발생() {
        // given
        String driverId = "driver123";
        String customerId = "customer456";
        Location location = new Location(37.5665, 126.9780);
        UpdateDriverLocationRequest request = new UpdateDriverLocationRequest(customerId, location);

        when(getStatusUseCase.getCurrentStatus(driverId)).thenReturn(TaxiDriverStatus.OFF_DUTY);

        // when & then
        assertThrows(OffDutyLocationUpdateException.class, () -> service.updateLocation(request, driverId));
        verify(sendLocationUseCase, never()).sendLocation(any(), any(), any(), any(), any());
    }

    @Test
    void 상태조회_실패시_예외발생() {
        // given
        String driverId = "driver123";
        String customerId = "customer456";
        Location location = new Location(37.5665, 126.9780);
        UpdateDriverLocationRequest request = new UpdateDriverLocationRequest(customerId, location);

        when(getStatusUseCase.getCurrentStatus(driverId)).thenThrow(new RuntimeException("상태 조회 실패"));

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updateLocation(request, driverId));
        assertEquals("상태 조회 실패", ex.getMessage());
        verify(sendLocationUseCase, never()).sendLocation(any(), any(), any(), any(), any());
    }

    @Test
    void 택시타입조회_실패시_예외발생() {
        // given
        String driverId = "driver123";
        String customerId = "customer456";
        Location location = new Location(37.5665, 126.9780);
        UpdateDriverLocationRequest request = new UpdateDriverLocationRequest(customerId, location);

        when(getStatusUseCase.getCurrentStatus(driverId)).thenReturn(TaxiDriverStatus.RESERVED);
        when(getTaxiTypeUseCase.getCurrentTaxiType(driverId)).thenThrow(new RuntimeException("타입 조회 실패"));

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updateLocation(request, driverId));
        assertEquals("타입 조회 실패", ex.getMessage());
        verify(sendLocationUseCase, never()).sendLocation(any(), any(), any(), any(), any());
    }
}