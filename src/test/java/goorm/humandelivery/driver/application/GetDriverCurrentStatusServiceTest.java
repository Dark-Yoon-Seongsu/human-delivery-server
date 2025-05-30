package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.out.LoadTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.global.exception.DriverEntityNotFoundException;
import goorm.humandelivery.shared.application.port.out.GetValuePort;
import goorm.humandelivery.shared.application.port.out.SetValueWithTtlPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//./gradlew test --tests "goorm.humandelivery.driver.application.GetDriverCurrentStatusServiceTest"
@ExtendWith(MockitoExtension.class)
class GetDriverCurrentStatusServiceTest {

    private GetValuePort getValuePort;
    private SetValueWithTtlPort setValueWithTtlPort;
    private LoadTaxiDriverPort loadTaxiDriverPort;
    private GetDriverCurrentStatusService service;

    @BeforeEach
    void setUp() {
        getValuePort = mock(GetValuePort.class);
        setValueWithTtlPort = mock(SetValueWithTtlPort.class);
        loadTaxiDriverPort = mock(LoadTaxiDriverPort.class);
        service = new GetDriverCurrentStatusService(getValuePort, setValueWithTtlPort, loadTaxiDriverPort);
    }

    @Test
    @DisplayName("Redis에 상태 정보가 있을 경우 해당 값을 반환한다")
    void getCurrentStatus_fromRedis() {
        when(getValuePort.getValue("taxidriver:driver1:status")).thenReturn("ON_DRIVING");

        TaxiDriverStatus status = service.getCurrentStatus("driver1");

        assertEquals(TaxiDriverStatus.ON_DRIVING, status);
        verify(loadTaxiDriverPort, never()).findByLoginId(any());
        verify(setValueWithTtlPort, never()).setValueWithTTL(any(), any(), any());
    }

    @Test
    @DisplayName("Redis에 없고 DB에 기사 정보가 있을 경우 DB에서 조회 후 Redis에 저장하고 반환한다")
    void getCurrentStatus_fromDbAndCacheIt() {
        when(getValuePort.getValue("taxidriver:driver1:status")).thenReturn(null);

        TaxiDriver mockDriver = mock(TaxiDriver.class);
        when(mockDriver.getStatus()).thenReturn(TaxiDriverStatus.AVAILABLE);
        when(loadTaxiDriverPort.findByLoginId("driver1")).thenReturn(Optional.of(mockDriver));

        TaxiDriverStatus status = service.getCurrentStatus("driver1");

        assertEquals(TaxiDriverStatus.AVAILABLE, status);
        verify(setValueWithTtlPort).setValueWithTTL("taxidriver:driver1:status", "AVAILABLE", Duration.ofHours(1));

    }

    @Test
    @DisplayName("Redis에도 없고 DB에도 기사 정보가 없으면 예외 발생")
    void getCurrentStatus_notFound() {
        when(getValuePort.getValue("taxidriver:driver1:status")).thenReturn(null);
        when(loadTaxiDriverPort.findByLoginId("driver1")).thenReturn(Optional.empty());

        assertThrows(DriverEntityNotFoundException.class, () -> service.getCurrentStatus("driver1"));
    }

    @Test
    @DisplayName("Redis에 잘못된 상태 문자열이 저장되어 있을 경우 예외 발생")
    void getCurrentStatus_invalidRedisStatus() {
        when(getValuePort.getValue("taxidriver:driver1:status")).thenReturn("INVALID_STATUS");

        assertThrows(IllegalArgumentException.class, () -> service.getCurrentStatus("driver1"));
    }
}