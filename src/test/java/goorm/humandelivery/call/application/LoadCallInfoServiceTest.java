package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.out.SaveCallInfoPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.dto.response.CallAcceptResponse;
import goorm.humandelivery.call.infrastructure.persistence.JpaCallInfoRepository;
import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.infrastructure.persistence.JpaCustomerRepository;
import goorm.humandelivery.global.exception.CallInfoEntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class LoadCallInfoServiceTest {

    @Autowired
    LoadCallInfoService loadCallInfoService;

    @Autowired
    SaveCallInfoPort saveCallInfoPort;

    @Autowired
    SaveCustomerPort saveCustomerPort;

    @Autowired
    JpaCallInfoRepository jpaCallInfoRepository;

    @Autowired
    JpaCustomerRepository jpaCustomerRepository;

    @AfterEach
    void tearDown() {
        jpaCallInfoRepository.deleteAllInBatch();
        jpaCustomerRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("콜 수락 응답 조회 테스트")
    class GetCallAcceptResponse {

        @Test
        @DisplayName("콜 ID를 통해 콜 수락 응답을 조회할 수 있다.")
        void getCallAcceptResponse() throws Exception {
            // Given
            Customer savedCustomer = saveCustomerPort.save(new Customer("testLoginId", "test", "test", "test"));
            CallInfo savedCallInfo = saveCallInfoPort.save(new CallInfo(null, savedCustomer, null, null, null));
            Long target = savedCallInfo.getId();

            // When
            CallAcceptResponse callAcceptResponse = loadCallInfoService.getCallAcceptResponse(target);

            // Then
            assertThat(callAcceptResponse.getCustomerLoginId()).isEqualTo("testLoginId");
        }

        @Test
        @DisplayName("존재하지 않는 콜 ID를 조회하면 예외가 발생한다.")
        void getCallAcceptResponseWithNotExistsCallId() throws Exception {
            // Given
            Long target = 999L;

            // When
            // Then
            assertThatThrownBy(() -> loadCallInfoService.getCallAcceptResponse(target))
                    .isInstanceOf(CallInfoEntityNotFoundException.class)
                    .hasMessage("아이디에 해당하는 CallInfo 엔티티가 존재하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("승객 로그인 아이디 조회 테스트")
    class FindCustomerLoginIdByCallId {

        @Test
        @DisplayName("콜 ID를 통해 승객 로그인 ID를 조회할 수 있다.")
        void findCustomerLoginIdByCallId() throws Exception {
            // Given
            Customer savedCustomer = saveCustomerPort.save(new Customer("testLoginId", "test", "test", "test"));
            CallInfo savedCallInfo = saveCallInfoPort.save(new CallInfo(null, savedCustomer, null, null, null));
            Long savedCallInfoId = savedCallInfo.getId();

            // When
            String target = loadCallInfoService.findCustomerLoginIdByCallId(savedCallInfoId);

            // Then
            assertThat(target).isEqualTo("testLoginId");
        }

        @Test
        @DisplayName("존재하지 않는 콜 ID를 조회하면 예외가 발생한다.")
        void findCustomerLoginIdByCallIdWithNotExistsCallId() throws Exception {
            // Given
            Long target = 999L;

            // When
            // Then
            assertThatThrownBy(() -> loadCallInfoService.getCallAcceptResponse(target))
                    .isInstanceOf(CallInfoEntityNotFoundException.class)
                    .hasMessage("아이디에 해당하는 CallInfo 엔티티가 존재하지 않습니다.");
        }
    }


}