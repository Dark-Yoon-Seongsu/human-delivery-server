package goorm.humandelivery.driver.application;

import goorm.humandelivery.call.application.port.out.RemoveRejectedDriversForCallPort;
import goorm.humandelivery.driver.application.port.in.DeleteAssignedCallUseCase;
import goorm.humandelivery.driver.application.port.in.HandleDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.out.*;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.global.exception.RedisKeyNotFoundException;
import goorm.humandelivery.shared.location.application.port.out.DeleteAllDriverLocationPort;
import goorm.humandelivery.shared.location.application.port.out.RemoveFromLocationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//./gradlew test --tests "goorm.humandelivery.driver.application.HandleDriverStatusServiceTest"
@ExtendWith(MockitoExtension.class)
class HandleDriverStatusServiceTest {

    private SetDriverStatusPort setDriverStatusPort;
    private SetDriverTaxiTypePort setDriverTaxiTypePort;
    private DeleteActiveDriverPort deleteActiveDriverPort;
    private DeleteAllDriverLocationPort deleteAllDriverLocationPort;
    private DeleteAssignedCallUseCase deleteAssignedCallUseCase;
    private RemoveFromLocationPort removeFromLocationPort;
    private GetAssignedCallPort getAssignedCallPort;
    private RemoveRejectedDriversForCallPort removeRejectedDriversForCallPort;
    private SetActiveDriverPort setActiveDriverPort;

    private HandleDriverStatusUseCase service;

    @BeforeEach
    void setUp() {
        setDriverStatusPort = mock(SetDriverStatusPort.class);
        setDriverTaxiTypePort = mock(SetDriverTaxiTypePort.class);
        deleteActiveDriverPort = mock(DeleteActiveDriverPort.class);
        deleteAllDriverLocationPort = mock(DeleteAllDriverLocationPort.class);
        deleteAssignedCallUseCase = mock(DeleteAssignedCallUseCase.class);
        removeFromLocationPort = mock(RemoveFromLocationPort.class);
        getAssignedCallPort = mock(GetAssignedCallPort.class);
        removeRejectedDriversForCallPort = mock(RemoveRejectedDriversForCallPort.class);
        setActiveDriverPort = mock(SetActiveDriverPort.class);

        service = new HandleDriverStatusService(
                setDriverStatusPort,
                setDriverTaxiTypePort,
                deleteActiveDriverPort,
                deleteAllDriverLocationPort,
                deleteAssignedCallUseCase,
                removeFromLocationPort,
                getAssignedCallPort,
                removeRejectedDriversForCallPort,
                setActiveDriverPort
        );
    }

    @Test
    @DisplayName("OFF_DUTY 상태 처리: active 목록 제외, 위치 삭제, 콜 삭제")
    void handleOffDutyStatus() {
        String driverId = "driver1";
        TaxiType type = TaxiType.NORMAL;

        var response = service.handleTaxiDriverStatusInRedis(driverId, TaxiDriverStatus.OFF_DUTY, type);

        assertEquals(TaxiDriverStatus.OFF_DUTY, response.getTaxiDriverStatus());

        verify(setDriverStatusPort).setDriverStatus(driverId, TaxiDriverStatus.OFF_DUTY);
        verify(setDriverTaxiTypePort).setDriverTaxiType(driverId, type);
        verify(deleteActiveDriverPort).setOffDuty(driverId);
        verify(deleteAllDriverLocationPort).deleteAllLocationData(driverId, type);
        verify(deleteAssignedCallUseCase).deleteCallBy(driverId);

        verifyNoMoreInteractions(removeFromLocationPort, getAssignedCallPort, removeRejectedDriversForCallPort, setActiveDriverPort);
    }

    @Test
    @DisplayName("AVAILABLE 상태 처리: 콜 삭제, 위치 삭제, active driver 등록")
    void handleAvailableStatus() {
        String driverId = "driver1";
        TaxiType type = TaxiType.NORMAL;

        var response = service.handleTaxiDriverStatusInRedis(driverId, TaxiDriverStatus.AVAILABLE, type);

        assertEquals(TaxiDriverStatus.AVAILABLE, response.getTaxiDriverStatus());

        verify(setDriverStatusPort).setDriverStatus(driverId, TaxiDriverStatus.AVAILABLE);
        verify(setDriverTaxiTypePort).setDriverTaxiType(driverId, type);

        verify(deleteAssignedCallUseCase).deleteCallBy(driverId);

        verify(removeFromLocationPort).removeFromLocation(driverId, type, TaxiDriverStatus.RESERVED);
        verify(removeFromLocationPort).removeFromLocation(driverId, type, TaxiDriverStatus.ON_DRIVING);

        verify(setActiveDriverPort).setActiveDriver(driverId);

        verifyNoMoreInteractions(deleteActiveDriverPort, deleteAllDriverLocationPort, getAssignedCallPort, removeRejectedDriversForCallPort);
    }

    @Test
    @DisplayName("RESERVED 상태 처리: 위치 삭제, 거부 택시기사 삭제, active driver 등록")
    void handleReservedStatus() {
        String driverId = "driver1";
        TaxiType type = TaxiType.NORMAL;

        String callId = "123";
        when(getAssignedCallPort.getCallIdByDriverId(driverId)).thenReturn(Optional.of(callId));

        var response = service.handleTaxiDriverStatusInRedis(driverId, TaxiDriverStatus.RESERVED, type);

        assertEquals(TaxiDriverStatus.RESERVED, response.getTaxiDriverStatus());

        verify(setDriverStatusPort).setDriverStatus(driverId, TaxiDriverStatus.RESERVED);
        verify(setDriverTaxiTypePort).setDriverTaxiType(driverId, type);

        verify(removeFromLocationPort).removeFromLocation(driverId, type, TaxiDriverStatus.AVAILABLE);
        verify(removeFromLocationPort).removeFromLocation(driverId, type, TaxiDriverStatus.ON_DRIVING);

        verify(getAssignedCallPort).getCallIdByDriverId(driverId);
        verify(removeRejectedDriversForCallPort).removeRejectedDrivers(Long.valueOf(callId));

        verify(setActiveDriverPort).setActiveDriver(driverId);

        verifyNoMoreInteractions(deleteActiveDriverPort, deleteAllDriverLocationPort, deleteAssignedCallUseCase);
    }

    @Test
    @DisplayName("ON_DRIVING 상태 처리: Redis에 콜 정보 없으면 예외 발생")
    void handleOnDrivingStatus_noCallInfo() {
        String driverId = "driver1";
        TaxiType type = TaxiType.NORMAL;

        when(getAssignedCallPort.getCallIdByDriverId(driverId)).thenReturn(Optional.empty());

        RedisKeyNotFoundException ex = assertThrows(RedisKeyNotFoundException.class,
                () -> service.handleTaxiDriverStatusInRedis(driverId, TaxiDriverStatus.ON_DRIVING, type));

        assertEquals("해당 키가 Redis 에 존재하지 않습니다. key : 현재 기사가 가진 콜 정보가 Redis 에 존재하지 않습니다.", ex.getMessage());


        verify(setDriverStatusPort).setDriverStatus(driverId, TaxiDriverStatus.ON_DRIVING);
        verify(setDriverTaxiTypePort).setDriverTaxiType(driverId, type);

        verify(getAssignedCallPort).getCallIdByDriverId(driverId);

        verifyNoMoreInteractions(deleteActiveDriverPort, deleteAllDriverLocationPort, deleteAssignedCallUseCase,
                removeFromLocationPort, removeRejectedDriversForCallPort, setActiveDriverPort);
    }

    @Test
    @DisplayName("ON_DRIVING 상태 처리: 콜 정보 있을 때 정상 처리")
    void handleOnDrivingStatus_withCallInfo() {
        String driverId = "driver1";
        TaxiType type = TaxiType.NORMAL;

        String callId = "123";
        when(getAssignedCallPort.getCallIdByDriverId(driverId)).thenReturn(Optional.of(callId));

        var response = service.handleTaxiDriverStatusInRedis(driverId, TaxiDriverStatus.ON_DRIVING, type);

        assertEquals(TaxiDriverStatus.ON_DRIVING, response.getTaxiDriverStatus());

        verify(setDriverStatusPort).setDriverStatus(driverId, TaxiDriverStatus.ON_DRIVING);
        verify(setDriverTaxiTypePort).setDriverTaxiType(driverId, type);

        verify(getAssignedCallPort).getCallIdByDriverId(driverId);

        verify(removeFromLocationPort).removeFromLocation(driverId, type, TaxiDriverStatus.AVAILABLE);
        verify(removeFromLocationPort).removeFromLocation(driverId, type, TaxiDriverStatus.RESERVED);

        verify(removeRejectedDriversForCallPort).removeRejectedDrivers(Long.valueOf(callId));

        verify(setActiveDriverPort).setActiveDriver(driverId);

        verifyNoMoreInteractions(deleteActiveDriverPort, deleteAllDriverLocationPort, deleteAssignedCallUseCase);
    }
}
