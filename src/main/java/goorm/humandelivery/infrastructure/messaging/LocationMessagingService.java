package goorm.humandelivery.infrastructure.messaging;

import goorm.humandelivery.common.exception.CustomerNotAssignedException;
import goorm.humandelivery.common.exception.OffDutyLocationUpdateException;
import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiDriverStatus;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.request.LocationResponse;
import goorm.humandelivery.infrastructure.redis.RedisService;
import goorm.humandelivery.infrastructure.redis.key.RedisKeyParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationMessagingService {

    private static final String LOCATION_TO_USER = "/queue/update-taxidriver-location";

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;


    public void sendLocation(String taxiDriverLoginId, TaxiDriverStatus status, TaxiType taxiType,
                             String customerLoginId, Location location) {

        log.info("[LocationMessagingService.sendLocation 호출] taxiDriverId: {}, status: {}, taxiType: {}, customerId: {}",
                taxiDriverLoginId, status, taxiType, customerLoginId);

        if (status == TaxiDriverStatus.OFF_DUTY) {
            throw new OffDutyLocationUpdateException();
        }

        // Redis에 위치 저장
        String locationKey = RedisKeyParser.getTaxiDriverLocationKeyBy(status, taxiType);
        redisService.setLocation(locationKey, taxiDriverLoginId, location);
        log.info("[위치 저장] taxiDriverId: {}, RedisKey: {}", taxiDriverLoginId, locationKey);

        // 위치 갱신 시간 저장
        String updateTimeKey = RedisKeyParser.taxiDriverLastUpdate(taxiDriverLoginId);
        redisService.setValueWithTTL(updateTimeKey, String.valueOf(System.currentTimeMillis()), Duration.ofMinutes(5));
        log.info("[위치 갱신 시간 저장] taxiDriverId: {}, RedisKey: {}", taxiDriverLoginId, updateTimeKey);

        // 사용자에게 위치 전송
        if (status == TaxiDriverStatus.RESERVED || status == TaxiDriverStatus.ON_DRIVING) {
            if (customerLoginId == null) {
                throw new CustomerNotAssignedException();
            }

            messagingTemplate.convertAndSendToUser(customerLoginId, LOCATION_TO_USER, new LocationResponse(location));
            log.info("[위치 전송 완료] to CustomerId: {}", customerLoginId);
        }
    }

}
