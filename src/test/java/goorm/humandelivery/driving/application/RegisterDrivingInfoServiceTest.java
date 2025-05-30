package goorm.humandelivery.driving.application;

import goorm.humandelivery.call.application.port.out.LoadMatchingPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driving.application.port.out.SaveDrivingInfoPort;
import goorm.humandelivery.driving.domain.DrivingInfo;
import goorm.humandelivery.driving.domain.DrivingStatus;
import goorm.humandelivery.driving.dto.request.CreateDrivingInfoRequest;
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
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterDrivingInfoServiceTest {

    @Mock
    private LoadMatchingPort loadMatchingPort;

    @Mock
    private SaveDrivingInfoPort saveDrivingInfoPort;

    @InjectMocks
    private RegisterDrivingInfoService registerDrivingInfoService;

    @Test
    @DisplayName("운행 정보 생성 성공")
    void createDrivingInfo_success() {
        // given
        Location departPosition = new Location(37.12345, 127.12345);
        CreateDrivingInfoRequest request = new CreateDrivingInfoRequest(1L, departPosition);

        Customer customer = Customer.builder()
                .loginId("customer1")
                .password("password")
                .name("고객1")
                .phoneNumber("010-1234-5678")
                .build();

        CallInfo callInfo = new CallInfo(
                1L, 
                customer, 
                new Location(37.0, 127.0), 
                new Location(37.5, 127.5), 
                TaxiType.NORMAL
        );

        Matching matching = Matching.builder()
                .id(1L)
                .callInfo(callInfo)
                .build();

        LocalDateTime pickupTime = LocalDateTime.now();
        DrivingInfo drivingInfoToSave = DrivingInfo.builder()
                .matching(matching)
                .origin(departPosition)
                .pickupTime(pickupTime)
                .drivingStatus(DrivingStatus.ON_DRIVING)
                .reported(false)
                .build();
        
        DrivingInfo savedDrivingInfo = DrivingInfo.builder()
                .matching(matching)
                .origin(departPosition)
                .pickupTime(pickupTime) 
                .drivingStatus(DrivingStatus.ON_DRIVING)
                .reported(false)
                .build();

        given(loadMatchingPort.findById(anyLong())).willReturn(Optional.of(matching));
        given(saveDrivingInfoPort.save(any(DrivingInfo.class))).willAnswer(invocation -> {
            DrivingInfo arg = invocation.getArgument(0);
            return DrivingInfo.builder()
                .matching(arg.getMatching())
                .origin(arg.getOrigin())
                .pickupTime(arg.getPickupTime())
                .drivingStatus(arg.getDrivingStatus())
                .reported(arg.isReported())
                .build();
        });

        // when
        DrivingInfo actualDrivingInfo = registerDrivingInfoService.create(request);

        // then
        assertThat(actualDrivingInfo.getMatching()).isEqualTo(savedDrivingInfo.getMatching());
        assertThat(actualDrivingInfo.getOrigin()).isEqualTo(savedDrivingInfo.getOrigin());
        assertThat(actualDrivingInfo.getPickupTime()).isCloseTo(savedDrivingInfo.getPickupTime(), within(200L, ChronoUnit.MILLIS));
        assertThat(actualDrivingInfo.getDrivingStatus()).isEqualTo(savedDrivingInfo.getDrivingStatus());
        assertThat(actualDrivingInfo.isReported()).isEqualTo(savedDrivingInfo.isReported());

        verify(loadMatchingPort).findById(request.getMatchingId());
        verify(saveDrivingInfoPort).save(any(DrivingInfo.class));
    }

    @Test
    @DisplayName("운행 정보 생성 실패 - 매칭 정보 없음")
    void createDrivingInfo_fail_matchingNotFound() {
        // given
        Location departPosition = new Location(37.12345, 127.12345);
        CreateDrivingInfoRequest request = new CreateDrivingInfoRequest(1L, departPosition);

        given(loadMatchingPort.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> registerDrivingInfoService.create(request))
                .isInstanceOf(MatchingEntityNotFoundException.class);

        verify(loadMatchingPort).findById(request.getMatchingId());
    }
} 