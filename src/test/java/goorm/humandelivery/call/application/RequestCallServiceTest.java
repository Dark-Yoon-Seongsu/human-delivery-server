package goorm.humandelivery.call.application;

import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.dto.request.CallMessageRequest;
import goorm.humandelivery.call.infrastructure.persistence.JpaCallInfoRepository;
import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.exception.CustomerNotFoundException;
import goorm.humandelivery.customer.infrastructure.persistence.JpaCustomerRepository;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.shared.application.port.out.MessageQueuePort;
import goorm.humandelivery.shared.location.domain.Location;
import goorm.humandelivery.shared.messaging.QueueMessage;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
class RequestCallServiceTest {

    @Autowired
    RequestCallService requestCallService;

    @Autowired
    SaveCustomerPort saveCustomerPort;

    @MockitoSpyBean
    MessageQueuePort messageQueuePort;

    @Autowired
    JpaCustomerRepository jpaCustomerRepository;

    @Autowired
    JpaCallInfoRepository jpaCallInfoRepository;

    @AfterEach
    void tearDown() {
        jpaCallInfoRepository.deleteAllInBatch();
        jpaCustomerRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("콜 요청 성공시 카프카 메시지 큐에 등록된다.")
    void requestCall() throws Exception {
        // Given
        Customer savedCustomer = saveCustomerPort.save(new Customer("test", "test", "test", "test"));
        Location expectedOrigin = new Location(38.2, 11.1);
        Location expectedDestination = new Location(39.3, 12.2);

        // When
        requestCallService.requestCall(new CallMessageRequest(expectedOrigin, expectedDestination, TaxiType.NORMAL, 0), savedCustomer.getLoginId());

        // Then
        List<CallInfo> callInfoResult = jpaCallInfoRepository.findAll();
        verify(messageQueuePort, timeout(5000)).enqueue(any(QueueMessage.class));
        assertThat(callInfoResult).hasSize(1)
                .extracting("expectedOrigin.latitude", "expectedDestination.latitude", "taxiType")
                .contains(Tuple.tuple(38.2, 39.3, TaxiType.NORMAL));
    }

    @Test
    @DisplayName("존재하지 않는 승객 로그인 ID로 콜 요청시 예외가 발생한다.")
    void requestCallWithNotExistsCustomerLoginId() throws Exception {
        // Given
        Location expectedOrigin = new Location(38.2, 11.1);
        Location expectedDestination = new Location(39.3, 12.2);

        // When
        // Then
        assertThatThrownBy(() -> requestCallService.requestCall(new CallMessageRequest(expectedOrigin, expectedDestination, TaxiType.NORMAL, 0), "exceptionLoginId"))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

}