package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.out.SaveCallInfoPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.dto.response.CallRejectResponse;
import goorm.humandelivery.call.infrastructure.persistence.JpaCallInfoRepository;
import goorm.humandelivery.driver.application.port.out.SaveTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.infrastructure.persistence.JpaTaxiDriverRepository;
import goorm.humandelivery.global.exception.CallInfoEntityNotFoundException;
import goorm.humandelivery.global.exception.TaxiDriverEntityNotFoundException;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class RejectCallServiceTest {

    @Autowired
    RejectCallService rejectCallService;

    @Autowired
    SaveCallInfoPort saveCallInfoPort;

    @Autowired
    SaveTaxiDriverPort saveTaxiDriverPort;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    JpaCallInfoRepository jpaCallInfoRepository;

    @Autowired
    JpaTaxiDriverRepository jpaTaxiDriverRepository;

    @AfterEach
    void tearDown() {
        jpaCallInfoRepository.deleteAllInBatch();
        jpaTaxiDriverRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("택시기사가 콜 요청을 거절하면 해당 콜의 거절 리스트에 추가된다.")
    void addRejectedDriverToCall() throws Exception {
        // Given
        TaxiDriver taxiDriver = TaxiDriver.builder().loginId("testLoginId").phoneNumber("010-0000-0000").build();
        TaxiDriver savedTaxiDriver = saveTaxiDriverPort.save(taxiDriver);
        CallInfo savedCallInfo = saveCallInfoPort.save(new CallInfo(null, null, null, null, null));

        Long testCallId = savedCallInfo.getId();
        String testDriverLoginId = savedTaxiDriver.getLoginId();

        // When
        CallRejectResponse response = rejectCallService.addRejectedDriverToCall(testCallId, testDriverLoginId);

        // Then
        String key = RedisKeyParser.getRejectCallKey(testCallId);
        Set<String> rejectedDrivers = stringRedisTemplate.opsForSet().members(key);

        assertThat(rejectedDrivers).contains(testDriverLoginId);
        assertThat(response.getCallId()).isEqualTo(testCallId);

        stringRedisTemplate.delete(key);
    }

    @Test
    @DisplayName("존재하지 않는 콜 ID로 요청 시 예외가 발생한다.")
    void addRejectedDriverToCallWithNotExistsCallInfo() throws Exception {
        // Given
        TaxiDriver taxiDriver = TaxiDriver.builder().loginId("testLoginId").phoneNumber("010-0000-0000").build();
        TaxiDriver savedTaxiDriver = saveTaxiDriverPort.save(taxiDriver);

        // When
        // Then
        assertThatThrownBy(() -> rejectCallService.addRejectedDriverToCall(999L, savedTaxiDriver.getLoginId()))
                .isInstanceOf(CallInfoEntityNotFoundException.class)
                .hasMessage("아이디에 해당하는 CallInfo 엔티티가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 택시기사 로그인 ID로 요청 시 예외가 발생한다.")
    void addRejectedDriverToCallWithNotExistsTaxiDriver() throws Exception {
        // Given
        CallInfo savedCallInfo = saveCallInfoPort.save(new CallInfo(null, null, null, null, null));

        // When
        // Then
        assertThatThrownBy(() -> rejectCallService.addRejectedDriverToCall(savedCallInfo.getId(), "expcetionId"))
                .isInstanceOf(TaxiDriverEntityNotFoundException.class)
                .hasMessage("아이디에 해당하는 TaxiDriver 엔티티가 존재하지 않습니다.");
    }
}