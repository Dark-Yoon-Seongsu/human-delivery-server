package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.out.NotifyDispatchSuccessToCustomerPort;
import goorm.humandelivery.call.application.port.out.SaveCallInfoPort;
import goorm.humandelivery.call.application.port.out.SetCallWithPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.domain.CallStatus;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.call.dto.request.CallAcceptRequest;
import goorm.humandelivery.call.infrastructure.persistence.JpaCallInfoRepository;
import goorm.humandelivery.call.infrastructure.persistence.JpaMatchingRepository;
import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.infrastructure.persistence.JpaCustomerRepository;
import goorm.humandelivery.driver.application.port.out.SaveTaxiDriverPort;
import goorm.humandelivery.driver.application.port.out.SaveTaxiPort;
import goorm.humandelivery.driver.application.port.out.SetDriverStatusPort;
import goorm.humandelivery.driver.domain.*;
import goorm.humandelivery.driver.infrastructure.persistence.JpaTaxiDriverRepository;
import goorm.humandelivery.driver.infrastructure.persistence.JpaTaxiRepository;
import goorm.humandelivery.global.exception.CallAlreadyCompletedException;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@SpringBootTest
class AcceptCallServiceTest {

    @Autowired
    AcceptCallService acceptCallService;

    @MockitoSpyBean
    NotifyDispatchSuccessToCustomerPort notifyDispatchSuccessToCustomerPort;

    @Autowired
    SaveCallInfoPort saveCallInfoPort;

    @Autowired
    SaveTaxiDriverPort saveTaxiDriverPort;

    @Autowired
    SaveTaxiPort saveTaxiPort;

    @Autowired
    SaveCustomerPort saveCustomerPort;

    @Autowired
    SetCallWithPort setCallWithPort;

    @Autowired
    SetDriverStatusPort setDriverStatusPort;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    JpaCallInfoRepository jpaCallInfoRepository;

    @Autowired
    JpaTaxiDriverRepository jpaTaxiDriverRepository;

    @Autowired
    JpaMatchingRepository jpaMatchingRepository;

    @Autowired
    JpaTaxiRepository jpaTaxiRepository;

    @Autowired
    JpaCustomerRepository jpaCustomerRepository;


    @AfterEach
    void tearDown() {
        jpaMatchingRepository.deleteAllInBatch();
        jpaCallInfoRepository.deleteAllInBatch();
        jpaTaxiDriverRepository.deleteAllInBatch();
        jpaTaxiRepository.deleteAllInBatch();
        jpaCustomerRepository.deleteAllInBatch();
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    @DisplayName("콜 수락 요청 시 매칭 생성 및 고객 알림까지 정상 수행된다.")
    void acceptCall() throws Exception {
        // Given
        Customer savedCustomer = saveCustomerPort.save(new Customer("testCustomerLoginId", "test", "test", "test"));
        CallInfo savedCallInfo = saveCallInfoPort.save(new CallInfo(null, savedCustomer, null, null, TaxiType.NORMAL));
        Taxi savedTaxi = saveTaxiPort.save(Taxi.builder().taxiType(TaxiType.NORMAL).fuelType(FuelType.DIESEL).build());
        TaxiDriver savedTaxiDriver = saveTaxiDriverPort.save(TaxiDriver.builder().loginId("testDriverLoginId").taxi(savedTaxi).phoneNumber("010-0000-0000").build());
        setDriverStatusPort.setDriverStatus(savedTaxiDriver.getLoginId(), TaxiDriverStatus.AVAILABLE);
        setCallWithPort.setCallWith(savedCallInfo.getId(), CallStatus.SENT);

        CallAcceptRequest callAcceptRequest = new CallAcceptRequest();
        callAcceptRequest.setCallId(savedCallInfo.getId());

        // When
        acceptCallService.acceptCall(callAcceptRequest, savedTaxiDriver.getLoginId());

        // Then
        verify(notifyDispatchSuccessToCustomerPort).sendToCustomer(anyString(), any());

        List<Matching> matchingResult = jpaMatchingRepository.findAll();
        assertThat(matchingResult).hasSize(1)
                .extracting("callInfo.id", "taxiDriver.id")
                .contains(Tuple.tuple(savedCallInfo.getId(), savedTaxiDriver.getId()));

        List<TaxiDriver> taxiDriverResult = jpaTaxiDriverRepository.findAll();
        assertThat(taxiDriverResult).hasSize(1)
                .extracting("status")
                .contains(TaxiDriverStatus.RESERVED);
    }

    @Test
    @DisplayName("이미 수락된 콜 요청을 수락하면 예외가 발생한다.")
    void acceptCallWithAlreadyAcceptedCall() throws Exception {
        // Given
        Customer savedCustomer = saveCustomerPort.save(new Customer("testCustomerLoginId", "test", "test", "test"));
        CallInfo savedCallInfo = saveCallInfoPort.save(new CallInfo(null, savedCustomer, null, null, TaxiType.NORMAL));
        Taxi savedTaxi = saveTaxiPort.save(Taxi.builder().taxiType(TaxiType.NORMAL).fuelType(FuelType.DIESEL).build());
        TaxiDriver savedTaxiDriver = saveTaxiDriverPort.save(TaxiDriver.builder().loginId("testDriverLoginId").taxi(savedTaxi).phoneNumber("010-0000-0000").build());
        setDriverStatusPort.setDriverStatus(savedTaxiDriver.getLoginId(), TaxiDriverStatus.AVAILABLE);
        setCallWithPort.setCallWith(savedCallInfo.getId(), CallStatus.SENT);

        CallAcceptRequest callAcceptRequest = new CallAcceptRequest();
        callAcceptRequest.setCallId(savedCallInfo.getId());
        acceptCallService.acceptCall(callAcceptRequest, savedTaxiDriver.getLoginId());

        // When
        // Then
        assertThatThrownBy(() -> acceptCallService.acceptCall(callAcceptRequest, savedTaxiDriver.getLoginId()))
                .isInstanceOf(CallAlreadyCompletedException.class)
                .hasMessage("이미 완료된 배차 요청입니다.");
    }

}