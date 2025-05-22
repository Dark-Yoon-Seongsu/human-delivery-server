package goorm.humandelivery.application;

import goorm.humandelivery.TestFixture.CallMessagingTestFixture;
import goorm.humandelivery.customer.service.CallMessageProcessingService;
import goorm.humandelivery.customer.service.CustomerService;
import goorm.humandelivery.domain.model.entity.Customer;
import goorm.humandelivery.domain.model.request.CallMessageRequest;
import goorm.humandelivery.domain.model.internal.CallMessage;
import goorm.humandelivery.infrastructure.messaging.kafka.KafkaMessageQueueService;
import goorm.humandelivery.customer.service.CallSaveService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

//./gradlew test --tests "goorm.humandelivery.application.CallMessageProcessingServiceTest"
class CallMessageProcessingServiceTest {

    private CustomerService customerService;
    private CallSaveService callSaveService;
    private KafkaMessageQueueService kafkaMessageQueueService;

    private CallMessageProcessingService service;

    @BeforeEach
    void setUp() {
        customerService = mock(CustomerService.class);
        callSaveService = mock(CallSaveService.class);
        kafkaMessageQueueService = mock(KafkaMessageQueueService.class);

        service = new CallMessageProcessingService(customerService, callSaveService, kafkaMessageQueueService);
    }

    @Test
    void processMessage_성공_케이스() {
        // given
        String senderId = "user123";
        Customer customer = CallMessagingTestFixture.buildCustomer(senderId);
        CallMessageRequest request = CallMessagingTestFixture.buildDefaultCallMessageRequest();

        when(customerService.findCustomerByLoginId(senderId)).thenReturn(customer);
        when(callSaveService.saveCallAndGetCallId(any())).thenReturn(100L);

        // when
        service.processMessage(request, senderId);

        // then
        verify(customerService).findCustomerByLoginId(senderId);
        verify(callSaveService).saveCallAndGetCallId(any());
        verify(kafkaMessageQueueService).enqueue(any(CallMessage.class));
    }

    @Test
    void processMessage_고객정보없음_예외() {
        // given
        String senderId = "not_exist_user";
        CallMessageRequest request = CallMessagingTestFixture.buildDefaultCallMessageRequest();

        when(customerService.findCustomerByLoginId(senderId))
                .thenThrow(new IllegalArgumentException("고객 정보를 찾을 수 없습니다."));

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.processMessage(request, senderId);
        });

        assertEquals("고객 정보를 찾을 수 없습니다.", ex.getMessage());
    }

    @Test
    void processMessage_저장실패_예외() {
        // given
        String senderId = "user123";
        Customer customer = CallMessagingTestFixture.buildCustomer(senderId);
        CallMessageRequest request = CallMessagingTestFixture.buildDefaultCallMessageRequest();

        when(customerService.findCustomerByLoginId(senderId)).thenReturn(customer);
        when(callSaveService.saveCallAndGetCallId(any()))
                .thenThrow(new IllegalArgumentException("콜 저장 실패"));

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.processMessage(request, senderId);
        });

        assertEquals("콜 저장 실패", ex.getMessage());
    }

    @Test
    void processMessage_큐등록실패_예외() {
        // given
        String senderId = "user123";
        Customer customer = CallMessagingTestFixture.buildCustomer(senderId);
        CallMessageRequest request = CallMessagingTestFixture.buildDefaultCallMessageRequest();

        when(customerService.findCustomerByLoginId(senderId)).thenReturn(customer);
        when(callSaveService.saveCallAndGetCallId(any())).thenReturn(101L);
        doThrow(new RuntimeException("Kafka 등록 실패"))
                .when(kafkaMessageQueueService).enqueue(any(CallMessage.class));

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.processMessage(request, senderId);
        });

        assertEquals("Kafka 등록 실패", ex.getMessage());
    }
}
