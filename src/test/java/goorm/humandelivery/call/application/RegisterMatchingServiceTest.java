package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.out.SaveCallInfoPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.call.dto.request.CreateMatchingRequest;
import goorm.humandelivery.call.infrastructure.persistence.JpaCallInfoRepository;
import goorm.humandelivery.call.infrastructure.persistence.JpaMatchingRepository;
import goorm.humandelivery.driver.application.port.out.SaveTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.infrastructure.persistence.JpaTaxiDriverRepository;
import goorm.humandelivery.global.exception.CallInfoEntityNotFoundException;
import goorm.humandelivery.global.exception.DriverEntityNotFoundException;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class RegisterMatchingServiceTest {

    @Autowired
    RegisterMatchingService registerMatchingService;

    @Autowired
    SaveTaxiDriverPort saveTaxiDriverPort;

    @Autowired
    SaveCallInfoPort saveCallInfoPort;

    @Autowired
    JpaMatchingRepository jpaMatchingRepository;

    @Autowired
    JpaTaxiDriverRepository jpaTaxiDriverRepository;

    @Autowired
    JpaCallInfoRepository jpaCallInfoRepository;

    @AfterEach
    void tearDown() {
        jpaMatchingRepository.deleteAllInBatch();
        jpaCallInfoRepository.deleteAllInBatch();
        jpaTaxiDriverRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("배차 등록 테스트")
    class Create {

        @Test
        @DisplayName("콜 ID와 택시기사 ID를 통해 배차를 등록할 수 있다.")
        void create() throws Exception {
            // Given
            TaxiDriver taxiDriver = TaxiDriver.builder().loginId("TEST@TEST.com").password("TEST").name("TEST").licenseCode("TEST").phoneNumber("010-0000-0000").build();
            TaxiDriver savedTaxiDriver = saveTaxiDriverPort.save(taxiDriver);
            CallInfo savedCallInfo = saveCallInfoPort.save(new CallInfo(null, null, null, null, null));

            CreateMatchingRequest createMatchingRequest = new CreateMatchingRequest(savedCallInfo.getId(), savedTaxiDriver.getId());

            // When
            registerMatchingService.create(createMatchingRequest);

            // Then
            List<Matching> result = jpaMatchingRepository.findAll();
            assertThat(result).hasSize(1)
                    .extracting("callInfo.id", "taxiDriver.id")
                    .contains(Tuple.tuple(savedCallInfo.getId(), savedTaxiDriver.getId()));
        }

        @Test
        @DisplayName("존재하지 않는 콜 아이디로 배차 등록 요청시 예외가 발생한다.")
        void createWithNotExistsCallInfo() throws Exception {
            // Given
            TaxiDriver taxiDriver = TaxiDriver.builder().loginId("TEST@TEST.com").password("TEST").name("TEST").licenseCode("TEST").phoneNumber("010-0000-0000").build();
            TaxiDriver savedTaxiDriver = saveTaxiDriverPort.save(taxiDriver);

            CreateMatchingRequest createMatchingRequest = new CreateMatchingRequest(999L, savedTaxiDriver.getId());

            // When
            // Then
            assertThatThrownBy(() -> registerMatchingService.create(createMatchingRequest))
                    .isInstanceOf(CallInfoEntityNotFoundException.class)
                    .hasMessage("아이디에 해당하는 CallInfo 엔티티가 존재하지 않습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 택시기사 아이디로 배차 등록 요청시 예외가 발생한다.")
        void createWithNotExistsTaxiDriver() throws Exception {
            // Given
            CallInfo savedCallInfo = saveCallInfoPort.save(new CallInfo(null, null, null, null, null));

            CreateMatchingRequest createMatchingRequest = new CreateMatchingRequest(savedCallInfo.getId(), 999L);

            // When
            // Then
            assertThatThrownBy(() -> registerMatchingService.create(createMatchingRequest))
                    .isInstanceOf(DriverEntityNotFoundException.class)
                    .hasMessage("아이디에 해당하는 엔티티가 존재하지 않습니다.");
        }
    }

}