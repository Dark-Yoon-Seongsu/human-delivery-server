package goorm.humandelivery.driving.application;

import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.call.application.port.out.SaveMatchingPort;
import goorm.humandelivery.call.application.port.out.SaveCallInfoPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driving.application.port.out.SaveDrivingInfoPort;
import goorm.humandelivery.driving.domain.DrivingInfo;
import goorm.humandelivery.driving.domain.DrivingStatus;
import goorm.humandelivery.driving.dto.request.CreateDrivingInfoRequest;
import goorm.humandelivery.global.exception.MatchingEntityNotFoundException;
import goorm.humandelivery.shared.location.domain.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RegisterDrivingInfoServiceIntegrationTest {

    @Autowired
    private RegisterDrivingInfoService registerDrivingInfoService;

    @Autowired
    private LoadMatchingPort loadMatchingPort;

    @Autowired
    private SaveMatchingPort saveMatchingPort;

    @Autowired
    private SaveCallInfoPort saveCallInfoPort;

    @Autowired
    private SaveDrivingInfoPort saveDrivingInfoPort;

    private Customer customer;
    private CallInfo callInfo;
    private TaxiDriver taxiDriver;
    private Matching matching;

    @BeforeEach
    void setUp() {
        // 고객 생성
        customer = Customer.builder()
                .loginId("testCustomer")
                .build();

        // 호출 정보 생성
        Location expectedOrigin = new Location(37.5665, 126.9780);
        Location expectedDestination = new Location(37.5006, 127.0366);
        callInfo = new CallInfo(null, customer, expectedOrigin, expectedDestination, TaxiType.NORMAL);

        // 택시 기사 생성
        taxiDriver = TaxiDriver.builder()
                .loginId("testDriver")
                .build();

        // 매칭 정보 생성
        matching = Matching.builder()
                .callInfo(callInfo)
                .taxiDriver(taxiDriver)
                .build();
        
        // CallInfo를 먼저 저장
        callInfo = saveCallInfoPort.save(callInfo);
        
        // 그 다음 Matching 저장
        matching = saveMatchingPort.save(matching);
    }

    @Nested
    @DisplayName("운행 정보 등록 통합 테스트")
    class RegisterDrivingInfoIntegrationTest {

        @Test
        @DisplayName("존재하지 않는 매칭 ID로 운행 정보 등록하면 예외가 발생한다.")
        void create_MatchingNotFound_ThrowsException() {
            // Given
            Long nonExistentMatchingId = 999999L;
            Location departPosition = new Location(37.5665, 126.9780);
            CreateDrivingInfoRequest request = new CreateDrivingInfoRequest(nonExistentMatchingId, departPosition);

            // When & Then
            assertThatThrownBy(() -> registerDrivingInfoService.create(request))
                    .isInstanceOf(MatchingEntityNotFoundException.class)
                    .hasMessage("해당 아이디를 가진 Matching 엔티티가 존재하지 않습니다.");
        }

        @Test
        @DisplayName("서울역에서 출발하는 운행 정보를 등록할 수 있다.")
        void create_SeoulStation_Success() {
            // Given
            Location seoulStation = new Location(37.5665, 126.9780);
            CreateDrivingInfoRequest request = new CreateDrivingInfoRequest(matching.getId(), seoulStation);

            // When
            DrivingInfo drivingInfo = registerDrivingInfoService.create(request);

            // Then
            assertThat(drivingInfo.getMatching()).isEqualTo(matching);
            assertThat(drivingInfo.getOrigin()).isEqualTo(seoulStation);
            assertThat(drivingInfo.getDrivingStatus()).isEqualTo(DrivingStatus.ON_DRIVING);
            assertThat(drivingInfo.isReported()).isFalse();
        }

        @Test
        @DisplayName("강남역에서 출발하는 운행 정보를 등록할 수 있다.")
        void create_GangnamStation_Success() {
            // Given
            Location gangnamStation = new Location(37.5006, 127.0366);
            CreateDrivingInfoRequest request = new CreateDrivingInfoRequest(matching.getId(), gangnamStation);

            // When
            DrivingInfo drivingInfo = registerDrivingInfoService.create(request);

            // Then
            assertThat(drivingInfo.getMatching()).isEqualTo(matching);
            assertThat(drivingInfo.getOrigin()).isEqualTo(gangnamStation);
            assertThat(drivingInfo.getDrivingStatus()).isEqualTo(DrivingStatus.ON_DRIVING);
            assertThat(drivingInfo.isReported()).isFalse();
        }
    }
} 