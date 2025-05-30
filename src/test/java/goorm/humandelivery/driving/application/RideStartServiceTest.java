package goorm.humandelivery.driving.application;

import goorm.humandelivery.call.application.port.out.LoadCallInfoPort;
import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.driver.application.port.in.ChangeTaxiDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.in.GetDriverCurrentTaxiTypeUseCase;
import goorm.humandelivery.driver.application.port.in.HandleDriverStatusUseCase;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driving.application.port.in.RegisterDrivingInfoUseCase;
import goorm.humandelivery.driving.application.port.out.SendDrivingStartToCustomerPort;
import goorm.humandelivery.driving.application.port.out.SendDrivingStartToDriverPort;
import goorm.humandelivery.driving.domain.DrivingInfo;
import goorm.humandelivery.driving.dto.request.CreateDrivingInfoRequest;
import goorm.humandelivery.driving.dto.response.DrivingInfoResponse;
import goorm.humandelivery.shared.location.application.port.in.GetDriverLocationUseCase;
import goorm.humandelivery.shared.location.domain.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class RideStartServiceTest {

    @Mock
    private LoadMatchingPort loadMatchingPort;

    @Mock
    private GetDriverLocationUseCase getDriverLocationUseCase;

    @Mock
    private RegisterDrivingInfoUseCase registerDrivingInfoUseCase;

    @Mock
    private ChangeTaxiDriverStatusUseCase changeTaxiDriverStatusUseCase;

    @Mock
    private GetDriverCurrentTaxiTypeUseCase getDriverCurrentTaxiTypeUseCase;

    @Mock
    private HandleDriverStatusUseCase handleDriverStatusUseCase;

    @Mock
    private LoadCallInfoPort loadCallInfoPort;

    @Mock
    private SendDrivingStartToCustomerPort sendDrivingStartToCustomerPort;

    @Mock
    private SendDrivingStartToDriverPort sendDrivingStartToDriverPort;

    @InjectMocks
    private RideStartService rideStartService;

    private Long callId;
    private String taxiDriverLoginId;
    private Long matchingId;
    private Location driverLocation;
    private DrivingInfo drivingInfo;
    private String customerLoginId;

    @BeforeEach
    void setUp() {
        callId = 1L;
        taxiDriverLoginId = "driver123";
        matchingId = 100L;
        driverLocation = new Location(37.5665, 126.9780);
        customerLoginId = "customer123";

        drivingInfo = DrivingInfo.builder()
                .origin(driverLocation)
                .pickupTime(java.time.LocalDateTime.now())
                .drivingStatus(goorm.humandelivery.driving.domain.DrivingStatus.ON_DRIVING)
                .reported(false)
                .build();
    }

    @Nested
    @DisplayName("승차 시작 처리 테스트")
    class RideStartTest {

        @Test
        @DisplayName("정상적인 승차 시작 프로세스가 성공한다.")
        void rideStart_Success() {
            // Given
            given(loadMatchingPort.findMatchingIdByCallInfoId(callId))
                    .willReturn(Optional.of(matchingId));
            given(getDriverLocationUseCase.getDriverLocation(taxiDriverLoginId))
                    .willReturn(driverLocation);
            given(registerDrivingInfoUseCase.create(any(CreateDrivingInfoRequest.class)))
                    .willReturn(drivingInfo);
            given(changeTaxiDriverStatusUseCase.changeStatus(taxiDriverLoginId, TaxiDriverStatus.ON_DRIVING))
                    .willReturn(TaxiDriverStatus.ON_DRIVING);
            given(getDriverCurrentTaxiTypeUseCase.getCurrentTaxiType(taxiDriverLoginId))
                    .willReturn(TaxiType.NORMAL);
            given(loadCallInfoPort.findCustomerLoginIdByCallId(callId))
                    .willReturn(Optional.of(customerLoginId));

            // When
            rideStartService.rideStart(callId, taxiDriverLoginId);

            // Then
            then(loadMatchingPort).should().findMatchingIdByCallInfoId(callId);
            then(getDriverLocationUseCase).should().getDriverLocation(taxiDriverLoginId);
            then(registerDrivingInfoUseCase).should().create(any(CreateDrivingInfoRequest.class));
            then(changeTaxiDriverStatusUseCase).should().changeStatus(taxiDriverLoginId, TaxiDriverStatus.ON_DRIVING);
            then(getDriverCurrentTaxiTypeUseCase).should().getCurrentTaxiType(taxiDriverLoginId);
            then(handleDriverStatusUseCase).should().handleTaxiDriverStatusInRedis(taxiDriverLoginId, TaxiDriverStatus.ON_DRIVING, TaxiType.NORMAL);
            then(loadCallInfoPort).should().findCustomerLoginIdByCallId(callId);
            then(sendDrivingStartToCustomerPort).should().sendToCustomer(eq(customerLoginId), any(DrivingInfoResponse.class));
            then(sendDrivingStartToDriverPort).should().sendToDriver(eq(taxiDriverLoginId), any(DrivingInfoResponse.class));
        }

        @Test
        @DisplayName("매칭이 없으면 예외가 발생한다.")
        void rideStart_MatchingNotFound_ThrowsException() {
            // Given
            given(loadMatchingPort.findMatchingIdByCallInfoId(callId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rideStartService.rideStart(callId, taxiDriverLoginId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 callId에 매칭이 없습니다.");
        }

        @Test
        @DisplayName("고객 정보가 없으면 예외가 발생한다.")
        void rideStart_CustomerNotFound_ThrowsException() {
            // Given
            given(loadMatchingPort.findMatchingIdByCallInfoId(callId))
                    .willReturn(Optional.of(matchingId));
            given(getDriverLocationUseCase.getDriverLocation(taxiDriverLoginId))
                    .willReturn(driverLocation);
            given(registerDrivingInfoUseCase.create(any(CreateDrivingInfoRequest.class)))
                    .willReturn(drivingInfo);
            given(changeTaxiDriverStatusUseCase.changeStatus(taxiDriverLoginId, TaxiDriverStatus.ON_DRIVING))
                    .willReturn(TaxiDriverStatus.ON_DRIVING);
            given(getDriverCurrentTaxiTypeUseCase.getCurrentTaxiType(taxiDriverLoginId))
                    .willReturn(TaxiType.NORMAL);
            given(loadCallInfoPort.findCustomerLoginIdByCallId(callId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rideStartService.rideStart(callId, taxiDriverLoginId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 callId의 고객 정보가 없습니다.");
        }

        @Test
        @DisplayName("기사 상태가 운행중으로 변경된다.")
        void rideStart_DriverStatusChangesToOnDriving() {
            // Given
            given(loadMatchingPort.findMatchingIdByCallInfoId(callId))
                    .willReturn(Optional.of(matchingId));
            given(getDriverLocationUseCase.getDriverLocation(taxiDriverLoginId))
                    .willReturn(driverLocation);
            given(registerDrivingInfoUseCase.create(any(CreateDrivingInfoRequest.class)))
                    .willReturn(drivingInfo);
            given(changeTaxiDriverStatusUseCase.changeStatus(taxiDriverLoginId, TaxiDriverStatus.ON_DRIVING))
                    .willReturn(TaxiDriverStatus.ON_DRIVING);
            given(getDriverCurrentTaxiTypeUseCase.getCurrentTaxiType(taxiDriverLoginId))
                    .willReturn(TaxiType.NORMAL);
            given(loadCallInfoPort.findCustomerLoginIdByCallId(callId))
                    .willReturn(Optional.of(customerLoginId));

            // When
            rideStartService.rideStart(callId, taxiDriverLoginId);

            // Then
            then(changeTaxiDriverStatusUseCase).should().changeStatus(taxiDriverLoginId, TaxiDriverStatus.ON_DRIVING);
            then(handleDriverStatusUseCase).should().handleTaxiDriverStatusInRedis(taxiDriverLoginId, TaxiDriverStatus.ON_DRIVING, TaxiType.NORMAL);
        }
    }
} 