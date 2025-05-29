package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.in.ChangeTaxiDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.in.GetDriverCurrentTaxiTypeUseCase;
import goorm.humandelivery.driver.application.port.in.HandleDriverStatusUseCase;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.request.UpdateTaxiDriverStatusRequest;
import goorm.humandelivery.driver.dto.response.UpdateTaxiDriverStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//./gradlew test --tests "goorm.humandelivery.driver.application.UpdateDriverStatusServiceTest"
@ExtendWith(MockitoExtension.class)
class UpdateDriverStatusServiceTest {

    private ChangeTaxiDriverStatusUseCase changeStatusUseCase;
    private HandleDriverStatusUseCase handleStatusUseCase;
    private GetDriverCurrentTaxiTypeUseCase getTaxiTypeUseCase;
    private UpdateDriverStatusService service;

    @BeforeEach
    void setUp() {
        changeStatusUseCase = mock(ChangeTaxiDriverStatusUseCase.class);
        handleStatusUseCase = mock(HandleDriverStatusUseCase.class);
        getTaxiTypeUseCase = mock(GetDriverCurrentTaxiTypeUseCase.class);

        service = new UpdateDriverStatusService(changeStatusUseCase, handleStatusUseCase, getTaxiTypeUseCase);
    }

    @Test
    void 정상적인_상태_변경_처리() {
        // given
        String driverId = "driver123";
        UpdateTaxiDriverStatusRequest request = new UpdateTaxiDriverStatusRequest("AVAILABLE");
        TaxiDriverStatus status = TaxiDriverStatus.AVAILABLE;
        TaxiType taxiType = TaxiType.NORMAL;
        UpdateTaxiDriverStatusResponse expectedResponse = new UpdateTaxiDriverStatusResponse(status);

        when(changeStatusUseCase.changeStatus(driverId, status)).thenReturn(status);
        when(getTaxiTypeUseCase.getCurrentTaxiType(driverId)).thenReturn(taxiType);
        when(handleStatusUseCase.handleTaxiDriverStatusInRedis(driverId, status, taxiType)).thenReturn(expectedResponse);

        // when
        UpdateTaxiDriverStatusResponse response = service.updateStatus(request, driverId);

        // then
        assertEquals(expectedResponse.getTaxiDriverStatus(), response.getTaxiDriverStatus());
        verify(changeStatusUseCase).changeStatus(driverId, status);
        verify(getTaxiTypeUseCase).getCurrentTaxiType(driverId);
        verify(handleStatusUseCase).handleTaxiDriverStatusInRedis(driverId, status, taxiType);
    }

    @Test
    void 잘못된_상태값이면_예외발생() {
        // given
        UpdateTaxiDriverStatusRequest request = new UpdateTaxiDriverStatusRequest("INVALID_STATUS");
        String driverId = "driver123";

        // when & then
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(request, driverId));
        verifyNoInteractions(changeStatusUseCase, getTaxiTypeUseCase, handleStatusUseCase);
    }

    @Test
    void 상태변경중_예외발생() {
        // given
        String driverId = "driver123";
        UpdateTaxiDriverStatusRequest request = new UpdateTaxiDriverStatusRequest("AVAILABLE");

        when(changeStatusUseCase.changeStatus(driverId, TaxiDriverStatus.AVAILABLE))
                .thenThrow(new RuntimeException("DB 변경 실패"));

        // when & then
        RuntimeException e = assertThrows(RuntimeException.class, () -> service.updateStatus(request, driverId));
        assertEquals("DB 변경 실패", e.getMessage());
        verify(changeStatusUseCase).changeStatus(driverId, TaxiDriverStatus.AVAILABLE);
        verifyNoInteractions(getTaxiTypeUseCase, handleStatusUseCase);
    }

    @Test
    void 택시타입조회_실패시_예외발생() {
        // given
        String driverId = "driver123";
        UpdateTaxiDriverStatusRequest request = new UpdateTaxiDriverStatusRequest("AVAILABLE");

        when(changeStatusUseCase.changeStatus(driverId, TaxiDriverStatus.AVAILABLE))
                .thenReturn(TaxiDriverStatus.AVAILABLE);
        when(getTaxiTypeUseCase.getCurrentTaxiType(driverId))
                .thenThrow(new RuntimeException("타입 조회 실패"));

        // when & then
        RuntimeException e = assertThrows(RuntimeException.class, () -> service.updateStatus(request, driverId));
        assertEquals("타입 조회 실패", e.getMessage());

        verify(changeStatusUseCase).changeStatus(driverId, TaxiDriverStatus.AVAILABLE);
        verify(getTaxiTypeUseCase).getCurrentTaxiType(driverId);
        verify(handleStatusUseCase, never()).handleTaxiDriverStatusInRedis(any(), any(), any());
    }
}
