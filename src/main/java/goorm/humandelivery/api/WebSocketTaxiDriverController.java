package goorm.humandelivery.api;

import goorm.humandelivery.application.DrivingInfoService;
import goorm.humandelivery.application.TaxiDriverService;
import goorm.humandelivery.call.application.LoadCallInfoService;
import goorm.humandelivery.call.dto.request.CallIdRequest;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.request.UpdateDriverLocationRequest;
import goorm.humandelivery.driver.dto.request.UpdateTaxiDriverStatusRequest;
import goorm.humandelivery.driver.dto.response.UpdateTaxiDriverStatusResponse;
import goorm.humandelivery.driving.domain.DrivingInfo;
import goorm.humandelivery.driving.dto.request.CreateDrivingInfoRequest;
import goorm.humandelivery.driving.dto.response.DrivingSummaryResponse;
import goorm.humandelivery.global.exception.OffDutyLocationUpdateException;
import goorm.humandelivery.shared.location.domain.Location;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;

@Slf4j
@Controller
@MessageMapping("/taxi-driver")  // "/app/taxi-driver"
@RequiredArgsConstructor
public class WebSocketTaxiDriverController {

    private final RedisService redisService;
    private final TaxiDriverService taxiDriverService;
    private final MessagingService messagingService;
    private final MatchingService matchingService;
    private final LoadCallInfoService loadCallInfoService;
    private final DrivingInfoService drivingInfoService;

    /**
     * 택시운전기사 상태 변경
     *
     * @param request
     * @param principal
     * @return UpdateTaxiDriverStatusResponse
     */
    @MessageMapping("/update-status")
    @SendToUser("/queue/taxi-driver-status")
    public UpdateTaxiDriverStatusResponse updateStatus(@Valid @RequestBody UpdateTaxiDriverStatusRequest request,
                                                       Principal principal) {

        String taxiDriverLoginId = principal.getName();
        String statusTobe = request.getStatus();
        log.info("[updateStatus 호출] taxiDriverId : {}, 상태 : {} 으로 변경요청", taxiDriverLoginId, statusTobe);

        // 1. DB에 상태 업데이트
        TaxiDriverStatus changedStatus = taxiDriverService.changeStatus(taxiDriverLoginId,
                TaxiDriverStatus.valueOf(statusTobe));

        // 2. 택시타입 조회
        TaxiType taxiType = taxiDriverService.findTaxiDriverTaxiType(taxiDriverLoginId).getTaxiType();

        // 3. redis 에 상태 업데이트
        return redisService.handleTaxiDriverStatusInRedis(taxiDriverLoginId, changedStatus, taxiType);
    }

    /**
     * 택시운전기사 위치정보 업데이트
     *
     * @param request
     * @param principal
     * @return UpdateLocationResponse
     */
    @MessageMapping("/update-location")
    public void updateLocation(UpdateDriverLocationRequest request, Principal principal) {
        String taxiDriverLoginId = principal.getName();
        String customerLoginId = request.getCustomerLoginId();
        Location location = request.getLocation();
        log.info("[updateLocation 호출] taxiDriverId : {}, 위도 : {}, 경도 : {}",
                principal.getName(),
                location.getLatitude(),
                location.getLongitude());

        // redis 에서 택시기사 상태조회 -> 없으면 DB 조회 -> redis 저장 -> 반환
        TaxiDriverStatus status = taxiDriverService.getCurrentTaxiDriverStatus(taxiDriverLoginId);

        // redis 에서 택시종류조회 -> 없으면 DB 조회 -> redis 저장 -> 반환
        TaxiType taxiType = taxiDriverService.getCurrentTaxiType(taxiDriverLoginId);

        if (status == TaxiDriverStatus.OFF_DUTY) {
            throw new OffDutyLocationUpdateException();
        }

        // 택시기사 위치정보 저장
        messagingService.sendLocation(taxiDriverLoginId, status, taxiType, customerLoginId, location);
    }

    /**
     * 승객 승차 완료 요청 처리
     */
    @MessageMapping("/ride-start")
    public void createDrivingInfo(CallIdRequest request, Principal principal) {

        log.info("[createDrivingInfo.WebSocketTaxiDriverController] 고객 승차.  콜 ID : {}, 택시기사 ID : {}", request.getCallId(),
                principal.getName());

        /**
         * TODO : 검증 필요. Matching Id와 callId가 같은지 확인해야함
         */

        Long callId = request.getCallId();
        String taxiDriverLoginId = principal.getName();


        // 해당 택시기사가

        /**
         * 손님 타고, 택시기사가 손님 탑승 확인 요청을 보냄. 이후 운행정보 엔티티 생성.
         */
        Long matchingId = matchingService.findMatchingIdByCallId(request.getCallId());
        Location taxiDriverLocation = redisService.getDriverLocation(principal.getName());

        CreateDrivingInfoRequest drivingInfoRequest = new CreateDrivingInfoRequest(matchingId,
                taxiDriverLocation);

        // 운행정보 엔티티 생성
        DrivingInfo savedDrivingInfo = drivingInfoService.create(drivingInfoRequest);

        // 택시 상태 변경
        TaxiDriverStatus changedStatus = taxiDriverService.changeStatus(taxiDriverLoginId,
                TaxiDriverStatus.ON_DRIVING);

        // 레디스 상태 변경
        TaxiType taxiType = redisService.getDriversTaxiType(taxiDriverLoginId);
        redisService.handleTaxiDriverStatusInRedis(taxiDriverLoginId, changedStatus, taxiType);

        // 운행 시작 메세지 전달
        String customerLoginId = loadCallInfoService.findCustomerLoginIdByCallId(callId);
        boolean isDrivingStarted = savedDrivingInfo.isDrivingStarted();

        // 응답 반환.
        messagingService.sendDrivingStartMessageToUser(customerLoginId, isDrivingStarted, false);
        messagingService.sendDrivingStartMessageToTaxiDriver(taxiDriverLoginId, isDrivingStarted, false);
    }

    /**
     * 승객 하차 완료 요청 처리 /app/taxi-driver/ride-finish
     */
    @MessageMapping("/ride-finish")
    public void finishDriving(CallIdRequest request, Principal principal) {

        /**
         * 손님이 하차했다. 드라이빙 인포 조회해서 상태 바꾸고, 택시기사 상태 바꿔야한다.
         * 그리고 손님과 택시기사에게 DrivingInfoResponse 를 전달해야 한다.
         */
        log.info("[finishDriving.WebSocketTaxiDriverController] 하차 요청.  콜 ID : {}, 택시기사 ID : {}", request.getCallId(),
                principal.getName());

        /**
         * TODO : DrivingInfo가 있는지 확인하고, 택시기사 확인하고, 고객 확인하고 -> 이후에 하차 요청 승인
         */

        Long callId = request.getCallId();
        String taxiDriverLoginId = principal.getName();


        Location location = redisService.getDriverLocation(taxiDriverLoginId);

        DrivingSummaryResponse response = drivingInfoService.finishDriving(callId, location);

        // 택시기사 상태 바꾸기 -> 빈차

        log.info("[finishDriving.WebSocketTaxiDriverController] 택시기사 DB 상태 바꾸기 호출 전.  콜 ID : {}, 택시기사 ID : {}", request.getCallId(),
                principal.getName());
        TaxiDriverStatus changedStatus = taxiDriverService.changeStatus(taxiDriverLoginId,
                TaxiDriverStatus.AVAILABLE);


        log.info("[finishDriving.WebSocketTaxiDriverController] 택시기사 REDIS 상태 바꾸기 호출 전.  콜 ID : {}, 택시기사 ID : {}", request.getCallId(),
                principal.getName());
        TaxiType taxiType = redisService.getDriversTaxiType(taxiDriverLoginId);
        redisService.handleTaxiDriverStatusInRedis(taxiDriverLoginId, changedStatus, taxiType);

        log.info("[finishDriving.WebSocketTaxiDriverController] 메세지 전송 전.  콜 ID : {}, 택시기사 ID : {}", request.getCallId(),
                principal.getName());
        messagingService.sendDrivingCompletedMessageToUser(response.getCustomerLoginId(), response);
        messagingService.sendDrivingCompletedMessageToTaxiDriver(taxiDriverLoginId, response);
    }
}
