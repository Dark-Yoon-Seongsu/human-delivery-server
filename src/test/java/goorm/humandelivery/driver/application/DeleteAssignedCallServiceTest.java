package goorm.humandelivery.driver.application;

import goorm.humandelivery.call.application.port.out.DeleteCallKeyDirectlyPort;
import goorm.humandelivery.call.application.port.out.DeleteCallStatusPort;
import goorm.humandelivery.call.application.port.out.RemoveRejectedDriversForCallPort;
import goorm.humandelivery.driver.application.port.out.DeleteAssignedCallPort;
import goorm.humandelivery.driver.application.port.out.GetAssignedCallPort;
import goorm.humandelivery.global.exception.InvalidCallIdFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

//./gradlew test --tests "goorm.humandelivery.driver.application.DeleteAssignedCallServiceTest"
class DeleteAssignedCallServiceTest {

    private GetAssignedCallPort getAssignedCallPort;
    private DeleteCallStatusPort deleteCallStatusPort;
    private DeleteAssignedCallPort deleteAssignedCallPort;
    private RemoveRejectedDriversForCallPort removeRejectedDriversForCallPort;
    private DeleteCallKeyDirectlyPort deleteCallKeyDirectlyPort;
    private DeleteAssignedCallService deleteAssignedCallService;

    @BeforeEach
    void setUp() {
        getAssignedCallPort = mock(GetAssignedCallPort.class);
        deleteCallStatusPort = mock(DeleteCallStatusPort.class);
        deleteAssignedCallPort = mock(DeleteAssignedCallPort.class);
        removeRejectedDriversForCallPort = mock(RemoveRejectedDriversForCallPort.class);
        deleteCallKeyDirectlyPort = mock(DeleteCallKeyDirectlyPort.class);

        deleteAssignedCallService = new DeleteAssignedCallService(
                getAssignedCallPort,
                deleteCallStatusPort,
                deleteAssignedCallPort,
                removeRejectedDriversForCallPort,
                deleteCallKeyDirectlyPort
        );
    }

    @Test
    @DisplayName("정상적인 콜 삭제")
    void deleteCall_success() {
        // given
        String loginId = "driver1";
        String callId = "123";

        when(getAssignedCallPort.getCallIdByDriverId(loginId)).thenReturn(Optional.of(callId));

        // when
        deleteAssignedCallService.deleteCallBy(loginId);

        // then
        verify(deleteCallStatusPort).deleteCallStatus(123L);
        verify(deleteAssignedCallPort).deleteAssignedCallOf(loginId);
        verify(deleteCallKeyDirectlyPort).deleteCallKey(123L);
        verify(removeRejectedDriversForCallPort).removeRejectedDrivers(123L);
    }

    @Test
    @DisplayName("콜 ID가 없는 경우 아무 작업도 하지 않음")
    void deleteCall_emptyCallId() {
        // given
        String loginId = "driver2";
        when(getAssignedCallPort.getCallIdByDriverId(loginId)).thenReturn(Optional.empty());

        // when
        deleteAssignedCallService.deleteCallBy(loginId);

        // then
        verify(deleteCallStatusPort, never()).deleteCallStatus(anyLong());
        verify(deleteAssignedCallPort, never()).deleteAssignedCallOf(anyString());
        verify(deleteCallKeyDirectlyPort, never()).deleteCallKey(anyLong());
        verify(removeRejectedDriversForCallPort, never()).removeRejectedDrivers(anyLong());
    }

    @Test
    @DisplayName("콜 ID가 숫자가 아닌 경우 InvalidCallIdFormatException 발생")
    void deleteCall_invalidCallIdFormat_throwsException() {
        // given
        String loginId = "driver3";
        when(getAssignedCallPort.getCallIdByDriverId(loginId)).thenReturn(Optional.of("abc"));

        // when & then
        InvalidCallIdFormatException exception = assertThrows(
                InvalidCallIdFormatException.class,
                () -> deleteAssignedCallService.deleteCallBy(loginId)
        );
        assertThat(exception.getMessage()).isEqualTo("올바르지 않은 콜 ID 형식입니다: abc");

        // 동작 수행되지 않아야 함
        verify(deleteCallStatusPort, never()).deleteCallStatus(anyLong());
        verify(deleteAssignedCallPort, never()).deleteAssignedCallOf(anyString());
        verify(deleteCallKeyDirectlyPort, never()).deleteCallKey(anyLong());
        verify(removeRejectedDriversForCallPort, never()).removeRejectedDrivers(anyLong());
    }

}
