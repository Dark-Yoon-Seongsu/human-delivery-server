package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.CancelCallService;
import goorm.humandelivery.call.application.port.out.LoadCallInfoPort;
import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.call.application.port.out.UpdateCallInfoPort;
import goorm.humandelivery.driver.application.UpdateDriverStatusService;
import goorm.humandelivery.driver.dto.request.UpdateTaxiDriverStatusRequest;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.global.exception.CallInfoEntityNotFoundException;
import goorm.humandelivery.global.exception.CancelCallNotAllowedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelCallServiceTest {

    @Mock
    private LoadCallInfoPort loadCallInfoPort;

    @Mock
    private LoadMatchingPort loadMatchingPort;

    @Mock
    private UpdateCallInfoPort updateCallInfoPort;

    @Mock
    private UpdateDriverStatusService updateDriverStatusService;

    @Mock
    private NotifyCallCancelToDriverService notifyCallCancelToDriverService;

    @Mock
    private DeleteMatchingService deleteMatchingService;

    @InjectMocks
    private CancelCallService cancelCallService;

//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }

    @Test
    @DisplayName("콜이 존재하지 않으면 CallInfoEntityNotFoundException 발생")
    void cancelCall_callInfoNotFound_throwException() {
        Long callId = 1L;
        // 명확하게 Optional.empty() 리턴
        when(loadCallInfoPort.findById(callId)).thenReturn(Optional.empty());

        assertThrows(CallInfoEntityNotFoundException.class, () -> cancelCallService.cancelCall(callId));
    }

    @Test
    @DisplayName("이미 취소된 콜은 아무 동작 없이 종료")
    void cancelCall_alreadyCancelled_doNothing() {
        Long callId = 1L;
        CallInfo callInfo = mock(CallInfo.class);
        when(callInfo.isCancelled()).thenReturn(true);
        when(loadCallInfoPort.findById(callId)).thenReturn(Optional.of(callInfo));

        cancelCallService.cancelCall(callId);

        verifyNoInteractions(loadMatchingPort, updateDriverStatusService, notifyCallCancelToDriverService, deleteMatchingService, updateCallInfoPort);
    }

    @Test
    @DisplayName("배달중인 기사 콜 취소 시 CancelCallNotAllowedException 발생")
    void cancelCall_driverOnDriving_throwCancelCallNotAllowedException() {
        Long callId = 1L;

        CallInfo callInfo = mock(CallInfo.class);
        when(callInfo.isCancelled()).thenReturn(false);
        // 여기 반드시 Optional.of(callInfo) 리턴
        when(loadCallInfoPort.findById(callId)).thenReturn(Optional.of(callInfo));

        TaxiDriver driver = mock(TaxiDriver.class);
        when(driver.getStatus()).thenReturn(TaxiDriverStatus.ON_DRIVING);

        Matching matching = mock(Matching.class);
        when(matching.getTaxiDriver()).thenReturn(driver);

        when(loadMatchingPort.findMatchingByCallInfoId(callId)).thenReturn(Optional.of(matching));

        CancelCallNotAllowedException ex = assertThrows(CancelCallNotAllowedException.class, () -> {
            cancelCallService.cancelCall(callId);
        });

        assertEquals("배달중인 콜은 취소할 수 없습니다.", ex.getMessage());

        verify(updateDriverStatusService, never()).updateStatus(any(), anyString());
        verify(notifyCallCancelToDriverService, never()).notifyDriverOfCancelledCall(anyString());
        verify(deleteMatchingService, never()).deleteByCallId(anyLong());
        verify(updateCallInfoPort, never()).cancel(any());
    }


    @Test
    @DisplayName("예약 상태 기사에 대해 콜 취소 정상 처리")
    void cancelCall_reservedDriver_cancelSuccessfully() {
        Long callId = 1L;

        CallInfo callInfo = mock(CallInfo.class);
        when(callInfo.isCancelled()).thenReturn(false);
        when(loadCallInfoPort.findById(callId)).thenReturn(Optional.of(callInfo));

        TaxiDriver driver = mock(TaxiDriver.class);
        when(driver.getStatus()).thenReturn(TaxiDriverStatus.RESERVED);
        when(driver.getLoginId()).thenReturn("driver1");

        Matching matching = mock(Matching.class);
        when(matching.getTaxiDriver()).thenReturn(driver);

        when(loadMatchingPort.findMatchingByCallInfoId(callId)).thenReturn(Optional.of(matching));

        cancelCallService.cancelCall(callId);

        verify(updateDriverStatusService).updateStatus(
                eq(new UpdateTaxiDriverStatusRequest(TaxiDriverStatus.AVAILABLE.getDescription())),
                eq("driver1")
        );
        verify(notifyCallCancelToDriverService).notifyDriverOfCancelledCall("driver1");
        verify(deleteMatchingService).deleteByCallId(callId);
        verify(callInfo).cancel();
        verify(updateCallInfoPort).cancel(callInfo);
    }

    @Test
    @DisplayName("매칭 정보 없으면 콜 취소만 수행")
    void cancelCall_noMatching_cancelCallInfoOnly() {
        Long callId = 1L;

        CallInfo callInfo = mock(CallInfo.class);
        when(callInfo.isCancelled()).thenReturn(false);
        when(loadCallInfoPort.findById(callId)).thenReturn(Optional.of(callInfo));

        when(loadMatchingPort.findMatchingByCallInfoId(callId)).thenReturn(Optional.empty());

        cancelCallService.cancelCall(callId);

        verify(updateDriverStatusService, never()).updateStatus(any(), anyString());
        verify(notifyCallCancelToDriverService, never()).notifyDriverOfCancelledCall(anyString());
        verify(deleteMatchingService, never()).deleteByCallId(anyLong());
        verify(callInfo).cancel();
        verify(updateCallInfoPort).cancel(callInfo);
    }


}
