package goorm.humandelivery.api;

import goorm.humandelivery.call.application.LoadCallInfoService;
import goorm.humandelivery.call.dto.request.CallIdRequest;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driving.domain.DrivingInfo;
import goorm.humandelivery.driving.dto.request.CreateDrivingInfoRequest;
import goorm.humandelivery.driving.dto.response.DrivingSummaryResponse;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

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
