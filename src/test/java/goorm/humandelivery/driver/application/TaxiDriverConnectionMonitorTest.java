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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

//./gradlew test --tests "goorm.humandelivery.driver.application.TaxiDriverConnectionMonitorTest"
@ExtendWith(MockitoExtension.class)
class TaxiDriverConnectionMonitorTest {

    @InjectMocks
    private TaxiDriverConnectionMonitor monitor;

    @Mock
    private GetActiveDriversPort getActiveDriversPort;
    @Mock private GetDriverStatusPort getDriverStatusPort;
    @Mock private GetDriverLastUpdatePort getDriverLastUpdatePort;
    @Mock private GetAssignedCallPort getAssignedCallPort;
    @Mock private GetDriverTaxiTypePort getDriverTaxiTypePort;
    @Mock private ChangeTaxiDriverStatusUseCase changeTaxiDriverStatusUseCase;
    @Mock private HandleDriverStatusUseCase handleDriverStatusUseCase;
    @Mock private DeleteMatchingUseCase deleteMatchingUseCase;
    @Mock private SendDispatchFailToDriverPort sendDispatchFailToDriverPort;
    @Mock private SendDispatchFailToCustomerPort sendDispatchFailToCustomerPort;
    @Mock private LoadCallInfoPort loadCallInfoPort;

    private final String driverId = "driver123";
    private final String customerLoginId = "customer1";
    private final String callId = "999";
    private final TaxiType taxiType = TaxiType.NORMAL;

    @BeforeEach
    void setUp() {

    }

    @Test
    void 위치_정상갱신된_기사는_아무작업도_하지_않는다() {
        long now = System.currentTimeMillis();
        when(getActiveDriversPort.getActiveDrivers()).thenReturn(Set.of(driverId));
        when(getDriverStatusPort.getDriverStatus(driverId)).thenReturn(TaxiDriverStatus.RESERVED);
        when(getDriverLastUpdatePort.getLastUpdate(driverId)).thenReturn(String.valueOf(now));

        monitor.monitorReservedTaxiDrivers();

        verifyNoInteractions(getAssignedCallPort);
        verifyNoInteractions(changeTaxiDriverStatusUseCase);
    }

    @Test
    void 위치미갱신_매칭정보없음이면_기사상태변경만_수행된다() {
        long outdated = System.currentTimeMillis() - 11_000;
        when(getActiveDriversPort.getActiveDrivers()).thenReturn(Set.of(driverId));
        when(getDriverStatusPort.getDriverStatus(driverId)).thenReturn(TaxiDriverStatus.RESERVED);
        when(getDriverLastUpdatePort.getLastUpdate(driverId)).thenReturn(String.valueOf(outdated));
        when(getAssignedCallPort.getCallIdByDriverId(driverId)).thenReturn(Optional.empty());
        when(getDriverTaxiTypePort.getDriverTaxiType(driverId)).thenReturn(taxiType);
        when(changeTaxiDriverStatusUseCase.changeStatus(driverId, TaxiDriverStatus.OFF_DUTY))
                .thenReturn(TaxiDriverStatus.OFF_DUTY);

        monitor.monitorReservedTaxiDrivers();

        verify(changeTaxiDriverStatusUseCase).changeStatus(driverId, TaxiDriverStatus.OFF_DUTY);
        verify(handleDriverStatusUseCase).handleTaxiDriverStatusInRedis(driverId, TaxiDriverStatus.OFF_DUTY, taxiType);
        verifyNoInteractions(sendDispatchFailToCustomerPort);
    }

    @Test
    void 위치미갱신_매칭정보존재시_상태변경_매칭삭제_메세지전송이_수행된다() {
        long outdated = System.currentTimeMillis() - 11_000;
        when(getActiveDriversPort.getActiveDrivers()).thenReturn(Set.of(driverId));
        when(getDriverStatusPort.getDriverStatus(driverId)).thenReturn(TaxiDriverStatus.RESERVED);
        when(getDriverLastUpdatePort.getLastUpdate(driverId)).thenReturn(String.valueOf(outdated));
        when(getAssignedCallPort.getCallIdByDriverId(driverId)).thenReturn(Optional.of(callId));
        when(loadCallInfoPort.findCustomerLoginIdByCallId(Long.valueOf(callId))).thenReturn(Optional.of(customerLoginId));
        when(getDriverTaxiTypePort.getDriverTaxiType(driverId)).thenReturn(taxiType);
        when(changeTaxiDriverStatusUseCase.changeStatus(driverId, TaxiDriverStatus.OFF_DUTY))
                .thenReturn(TaxiDriverStatus.OFF_DUTY);

        monitor.monitorReservedTaxiDrivers();

        verify(deleteMatchingUseCase).deleteByCallId(Long.valueOf(callId));
        verify(changeTaxiDriverStatusUseCase).changeStatus(driverId, TaxiDriverStatus.OFF_DUTY);
        verify(handleDriverStatusUseCase).handleTaxiDriverStatusInRedis(driverId, TaxiDriverStatus.OFF_DUTY, taxiType);
        verify(sendDispatchFailToCustomerPort).sendToCustomer(eq(customerLoginId), any(ErrorResponse.class));
        verify(sendDispatchFailToDriverPort).sendToDriver(eq(driverId), any(ErrorResponse.class));
    }


    @Test
    void 위치갱신값이_숫자가_아닐경우_NumberFormatException_무시하고_다음기사_처리한다() {
        String driver2 = "driver456";

        when(getActiveDriversPort.getActiveDrivers()).thenReturn(Set.of(driverId, driver2));
        when(getDriverStatusPort.getDriverStatus(anyString())).thenReturn(TaxiDriverStatus.RESERVED);
        when(getDriverStatusPort.getDriverStatus(driver2)).thenReturn(TaxiDriverStatus.RESERVED);

        when(getDriverLastUpdatePort.getLastUpdate(driverId)).thenReturn("not-a-number"); // NFE 발생
        when(getDriverLastUpdatePort.getLastUpdate(driver2)).thenReturn(String.valueOf(System.currentTimeMillis() - 11_000));

        when(getAssignedCallPort.getCallIdByDriverId(driver2)).thenReturn(Optional.empty());
        when(getDriverTaxiTypePort.getDriverTaxiType(driver2)).thenReturn(taxiType);

        // 💡 이 부분이 핵심
        when(changeTaxiDriverStatusUseCase.changeStatus(driver2, TaxiDriverStatus.OFF_DUTY))
                .thenReturn(TaxiDriverStatus.OFF_DUTY);

        monitor.monitorReservedTaxiDrivers();

        // driverId 는 skip 되었고, driver2 는 정상 처리됨
        verify(changeTaxiDriverStatusUseCase).changeStatus(driver2, TaxiDriverStatus.OFF_DUTY);
        verify(handleDriverStatusUseCase).handleTaxiDriverStatusInRedis(eq(driver2), eq(TaxiDriverStatus.OFF_DUTY), eq(taxiType));
    }

    @Test
    void getDriverTaxiType이_null이면_스킵된다() {
        when(getActiveDriversPort.getActiveDrivers()).thenReturn(Set.of("driver1"));
        when(getDriverStatusPort.getDriverStatus("driver1")).thenReturn(TaxiDriverStatus.RESERVED);
        when(getDriverLastUpdatePort.getLastUpdate("driver1")).thenReturn(String.valueOf(System.currentTimeMillis() - 11000));
        when(getAssignedCallPort.getCallIdByDriverId("driver1")).thenReturn(Optional.empty());
        when(getDriverTaxiTypePort.getDriverTaxiType("driver1")).thenReturn(null); // 핵심

        monitor.monitorReservedTaxiDrivers();

        verify(changeTaxiDriverStatusUseCase, never()).changeStatus(any(), any());
    }

    @Test
    void 고객정보없으면_예외발생하고_다음기사_처리된다() {
        String driverId = "driver1";
        when(getActiveDriversPort.getActiveDrivers()).thenReturn(Set.of(driverId));
        when(getDriverStatusPort.getDriverStatus(driverId)).thenReturn(TaxiDriverStatus.RESERVED);
        when(getDriverLastUpdatePort.getLastUpdate(driverId)).thenReturn(String.valueOf(System.currentTimeMillis() - 11000));
        when(getAssignedCallPort.getCallIdByDriverId(driverId)).thenReturn(Optional.of("123"));
        when(getDriverTaxiTypePort.getDriverTaxiType(driverId)).thenReturn(TaxiType.NORMAL);
        when(loadCallInfoPort.findCustomerLoginIdByCallId(123L)).thenReturn(Optional.empty());

        monitor.monitorReservedTaxiDrivers();

        verify(sendDispatchFailToCustomerPort, never()).sendToCustomer(any(), any());
        verify(deleteMatchingUseCase, never()).deleteByCallId(any());
    }

    @Test
    void deleteMatchingUseCase_예외발생해도_상태변경_진행된다() {
        String driverId = "driver1";
        given(changeTaxiDriverStatusUseCase.changeStatus(eq("driver1"), eq(TaxiDriverStatus.OFF_DUTY)))
                .willReturn(TaxiDriverStatus.OFF_DUTY);

        when(getActiveDriversPort.getActiveDrivers()).thenReturn(Set.of(driverId));
        when(getDriverStatusPort.getDriverStatus(driverId)).thenReturn(TaxiDriverStatus.RESERVED);
        when(getDriverLastUpdatePort.getLastUpdate(driverId)).thenReturn(String.valueOf(System.currentTimeMillis() - 11000));
        when(getAssignedCallPort.getCallIdByDriverId(driverId)).thenReturn(Optional.of("123"));
        when(getDriverTaxiTypePort.getDriverTaxiType(driverId)).thenReturn(TaxiType.NORMAL);
        when(loadCallInfoPort.findCustomerLoginIdByCallId(123L)).thenReturn(Optional.of("customer1"));
        doThrow(new RuntimeException("DB 장애")).when(deleteMatchingUseCase).deleteByCallId(123L);

        monitor.monitorReservedTaxiDrivers();

        verify(changeTaxiDriverStatusUseCase).changeStatus(driverId, TaxiDriverStatus.OFF_DUTY);
        verify(handleDriverStatusUseCase).handleTaxiDriverStatusInRedis(eq(driverId), eq(TaxiDriverStatus.OFF_DUTY), eq(TaxiType.NORMAL));
    }

    @Test
    void sendToCustomer_예외발생해도_드라이버전송은_진행된다() {
        String driverId = "driver1";
        when(getActiveDriversPort.getActiveDrivers()).thenReturn(Set.of(driverId));
        when(getDriverStatusPort.getDriverStatus(driverId)).thenReturn(TaxiDriverStatus.RESERVED);
        when(getDriverLastUpdatePort.getLastUpdate(driverId)).thenReturn(String.valueOf(System.currentTimeMillis() - 11000));
        when(getAssignedCallPort.getCallIdByDriverId(driverId)).thenReturn(Optional.of("123"));
        when(getDriverTaxiTypePort.getDriverTaxiType(driverId)).thenReturn(TaxiType.NORMAL);
        when(loadCallInfoPort.findCustomerLoginIdByCallId(123L)).thenReturn(Optional.of("customer1"));
        doThrow(new RuntimeException("네트워크 장애")).when(sendDispatchFailToCustomerPort)
                .sendToCustomer(eq("customer1"), any());

        monitor.monitorReservedTaxiDrivers();

        verify(sendDispatchFailToDriverPort).sendToDriver(eq(driverId), any());
    }

}
