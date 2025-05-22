package goorm.humandelivery.infrastructure.messaging.handler;

import goorm.humandelivery.application.CallInfoService;
import goorm.humandelivery.common.exception.NoAvailableTaxiException;
import goorm.humandelivery.domain.model.internal.CallMessage;
import goorm.humandelivery.infrastructure.messaging.MessagingService;
import goorm.humandelivery.service.CallRequestDispatchService;
import goorm.humandelivery.service.NearTaxiSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

//./gradlew test --tests "goorm.humandelivery.infrastructure.messaging.handler.CallMessageHandlerTest"
class CallMessageHandlerTest {

    private NearTaxiSearchService nearTaxiSearchService;
    private CallInfoService callInfoService;
    private MessagingService messagingService;
    private CallRequestDispatchService callRequestDispatchService;

    private CallMessageHandler handler;

    @BeforeEach
    void setUp() {
        nearTaxiSearchService = mock(NearTaxiSearchService.class);
        callInfoService = mock(CallInfoService.class);
        messagingService = mock(MessagingService.class);
        callRequestDispatchService = mock(CallRequestDispatchService.class);

        handler = new CallMessageHandler(
                nearTaxiSearchService,
                callInfoService,
                messagingService,
                callRequestDispatchService
        );
    }

    @Test
    void handle_dispatchesCall_whenTaxisAreAvailable() {
        // given
        CallMessage message = new CallMessage();
        message.setCallId(123L);
        message.setCustomerLoginId("customer-abc");

        List<String> drivers = List.of("driver1", "driver2");

        when(nearTaxiSearchService.findNearByAvailableDrivers(message)).thenReturn(drivers);

        // when
        handler.handle(message);

        // then
        verify(callRequestDispatchService).dispatchCallRequest(message, drivers);
        verify(callInfoService, never()).deleteCallById(any());
        verify(messagingService, never()).notifyDispatchFailedToCustomer(any());
    }

    @Test
    void handle_throwsException_whenNoTaxisAvailable() {
        // given
        CallMessage message = new CallMessage();
        message.setCallId(456L);
        message.setCustomerLoginId("customer-xyz");

        when(nearTaxiSearchService.findNearByAvailableDrivers(message)).thenReturn(List.of());

        // when & then
        assertThrows(NoAvailableTaxiException.class, () -> handler.handle(message));

        verify(callInfoService).deleteCallById(456L);
        verify(messagingService).notifyDispatchFailedToCustomer("customer-xyz");
        verify(callRequestDispatchService, never()).dispatchCallRequest(any(), any());
    }
}
