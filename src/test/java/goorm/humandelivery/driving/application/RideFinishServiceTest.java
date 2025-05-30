package goorm.humandelivery.driving.application;

import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driving.application.port.out.LoadDrivingInfoPort;
import goorm.humandelivery.driving.application.port.out.LoadDrivingSummaryPort;
import goorm.humandelivery.driving.application.port.out.SaveDrivingInfoPort;
import goorm.humandelivery.driving.domain.DrivingInfo;
import goorm.humandelivery.driving.domain.DrivingStatus;
import goorm.humandelivery.driving.dto.response.DrivingSummaryResponse;
import goorm.humandelivery.global.exception.DrivingInfoEntityNotFoundException;
import goorm.humandelivery.global.exception.MatchingEntityNotFoundException;
import goorm.humandelivery.shared.location.domain.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RideFinishServiceTest {

    @Mock
    private LoadMatchingPort loadMatchingPort;

    @Mock
    private LoadDrivingInfoPort loadDrivingInfoPort;

    @Mock
    private SaveDrivingInfoPort saveDrivingInfoPort;

    @Mock
    private LoadDrivingSummaryPort loadDrivingSummaryPort;

    @InjectMocks
    private FinishDrivingService finishDrivingService;

    @Test
    @DisplayName("운행 종료 성공")
    void finishDriving_success() {
        // given
        Long callId = 1L;
        Location destination = new Location(37.54321, 127.54321);

        Customer customer = Customer.builder()
                .loginId("customer1")
                .password("password")
                .name("고객1")
                .phoneNumber("010-1111-2222")
                .build();

        CallInfo callInfo = new CallInfo(
                callId,
                customer,
                new Location(37.12345, 127.12345),
                destination,
                TaxiType.NORMAL
        );

        TaxiDriver taxiDriver = TaxiDriver.builder()
                .loginId("driver1")
                .build();

        Matching matching = Matching.builder()
                .callInfo(callInfo)
                .taxiDriver(taxiDriver)
                .build();

        DrivingInfo drivingInfo = DrivingInfo.builder()
                .matching(matching)
                .origin(new Location(37.12345, 127.12345))
                .pickupTime(LocalDateTime.now().minusMinutes(10))
                .drivingStatus(DrivingStatus.ON_DRIVING)
                .reported(false)
                .build();
        
        LocalDateTime arrivingTimeForTest = LocalDateTime.now();

        DrivingSummaryResponse expectedResponse = new DrivingSummaryResponse(
                callId,
                customer.getLoginId(),
                taxiDriver.getLoginId(),
                drivingInfo.getOrigin(),
                drivingInfo.getPickupTime(),
                destination,
                arrivingTimeForTest,
                DrivingStatus.COMPLETE,
                false
        );

        given(loadMatchingPort.findMatchingByCallInfoId(callId)).willReturn(Optional.of(matching));
        given(loadDrivingInfoPort.findDrivingInfoByMatching(matching)).willReturn(Optional.of(drivingInfo));
        given(saveDrivingInfoPort.save(any(DrivingInfo.class))).willAnswer(invocation -> {
            DrivingInfo saved = invocation.getArgument(0);
            saved.finishDriving(destination, arrivingTimeForTest);
            return saved;
        });
        given(loadDrivingSummaryPort.findDrivingSummaryResponse(any(DrivingInfo.class))).willReturn(Optional.of(expectedResponse));

        // when
        DrivingSummaryResponse actualResponse = finishDrivingService.finishDriving(callId, destination);

        // then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getCallId()).isEqualTo(expectedResponse.getCallId());
        assertThat(actualResponse.getDestination().getLatitude()).isEqualTo(expectedResponse.getDestination().getLatitude());
        assertThat(actualResponse.getDestination().getLongitude()).isEqualTo(expectedResponse.getDestination().getLongitude());
        assertThat(actualResponse.getDrivingStatus()).isEqualTo(DrivingStatus.COMPLETE);

        verify(loadMatchingPort).findMatchingByCallInfoId(callId);
        verify(loadDrivingInfoPort).findDrivingInfoByMatching(matching);
        verify(saveDrivingInfoPort).save(any(DrivingInfo.class));
        verify(loadDrivingSummaryPort).findDrivingSummaryResponse(any(DrivingInfo.class));
    }

    @Test
    @DisplayName("운행 종료 실패 - 매칭 정보 없음")
    void finishDriving_fail_matchingNotFound() {
        // given
        Long callId = 1L;
        Location destination = new Location(37.54321, 127.54321);

        given(loadMatchingPort.findMatchingByCallInfoId(callId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> finishDrivingService.finishDriving(callId, destination))
                .isInstanceOf(MatchingEntityNotFoundException.class);

        verify(loadMatchingPort).findMatchingByCallInfoId(callId);
    }

    @Test
    @DisplayName("운행 종료 실패 - 운행 정보 없음")
    void finishDriving_fail_drivingInfoNotFound() {
        // given
        Long callId = 1L;
        Location destination = new Location(37.54321, 127.54321);

        Customer customer = Customer.builder()
            .loginId("customer1")
            .password("password")
            .name("고객1")
            .phoneNumber("010-1111-2222")
            .build();
        CallInfo callInfo = new CallInfo(callId, customer, new Location(37.0, 127.0), destination, TaxiType.NORMAL);
        Matching matching = Matching.builder().callInfo(callInfo).build();

        given(loadMatchingPort.findMatchingByCallInfoId(callId)).willReturn(Optional.of(matching));
        given(loadDrivingInfoPort.findDrivingInfoByMatching(matching)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> finishDrivingService.finishDriving(callId, destination))
                .isInstanceOf(DrivingInfoEntityNotFoundException.class);

        verify(loadMatchingPort).findMatchingByCallInfoId(callId);
        verify(loadDrivingInfoPort).findDrivingInfoByMatching(matching);
    }
} 