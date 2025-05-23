package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.in.SendDriverLocationToCustomerUseCase;
import goorm.humandelivery.driver.application.port.out.SendDriverLocationToCustomerPort;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.global.exception.CustomerNotAssignedException;
import goorm.humandelivery.global.exception.OffDutyLocationUpdateException;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import goorm.humandelivery.shared.application.port.out.SetValueWithTtlPort;
import goorm.humandelivery.shared.location.application.port.out.SetLocationPort;
import goorm.humandelivery.shared.location.domain.Location;
import goorm.humandelivery.driver.dto.response.DriverLocationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendDriverLocationToCustomerService implements SendDriverLocationToCustomerUseCase {

    private final SetLocationPort setLocationPort;
    private final SetValueWithTtlPort setValueWithTtlPort;
    private final SendDriverLocationToCustomerPort sendToCustomerPort;

    @Override
    public void sendLocation(String taxiDriverLoginId, TaxiDriverStatus status, TaxiType taxiType, String customerLoginId, Location location) {

        log.info("[MessagingService sendMessage : 호출] 택시기사아이디 : {}, 택시기사상태 : {}, 택시타입 : {},  고객아이디 : {} ",
                taxiDriverLoginId, status, taxiType, customerLoginId);

        DriverLocationResponse response = new DriverLocationResponse(location);

        if (status == TaxiDriverStatus.OFF_DUTY) {
            throw new OffDutyLocationUpdateException();
        }

        String locationKey = RedisKeyParser.getTaxiDriverLocationKeyBy(status, taxiType);
        setLocationPort.setLocation(locationKey, taxiDriverLoginId, location);
        log.info("[MessagingService sendMessage : 위치정보 저장] 택시기사아이디 : {}, 레디스 키 : {} ", taxiDriverLoginId, locationKey);

        String currentTime = String.valueOf(System.currentTimeMillis());
        String updateTimeKey = RedisKeyParser.taxiDriverLastUpdate(taxiDriverLoginId);
        setValueWithTtlPort.setValueWithTTL(updateTimeKey, currentTime, Duration.ofMinutes(5));
        log.info("[MessagingService sendMessage : 위치정보 갱신시간 저장] 택시기사아이디 : {}, 레디스 키 : {} ", taxiDriverLoginId, updateTimeKey);

        if (status == TaxiDriverStatus.RESERVED || status == TaxiDriverStatus.ON_DRIVING) {
            log.info("[MessagingService sendMessage => 유저에게 위치 전송] 택시기사아이디 : {}, 택시기사상태 : {}, 택시타입 : {},  고객아이디 : {} ",
                    taxiDriverLoginId, status, taxiType, customerLoginId);

            if (customerLoginId == null) {
                throw new CustomerNotAssignedException();
            }

            sendToCustomerPort.sendToCustomer(customerLoginId, response);
        }
    }
}