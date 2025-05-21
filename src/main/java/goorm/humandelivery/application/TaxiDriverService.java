package goorm.humandelivery.application;

import goorm.humandelivery.domain.repository.TaxiDriverRepository;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.response.TaxiTypeResponse;
import goorm.humandelivery.global.exception.TaxiDriverEntityNotFoundException;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TaxiDriverService {

    private final TaxiDriverRepository taxiDriverRepository;
    private final RedisService redisService;

    public TaxiDriverService(TaxiDriverRepository taxiDriverRepository,
                             RedisService redisService) {
        this.taxiDriverRepository = taxiDriverRepository;
        this.redisService = redisService;
    }

    @Transactional
    public TaxiDriverStatus changeStatus(String loginId, TaxiDriverStatus status) {

        log.info("[changeStatus.taxiDriverRepository.findByLoginId] 택시기사 조회. 택시기사 ID : {}", loginId);
        TaxiDriver taxiDriver = taxiDriverRepository.findByLoginId(loginId)
                .orElseThrow(TaxiDriverEntityNotFoundException::new);

        return taxiDriver.changeStatus(status);
    }

    public TaxiTypeResponse findTaxiDriverTaxiType(String loginId) {
        return taxiDriverRepository.findTaxiDriversTaxiTypeByLoginId(loginId)
                .orElseThrow(TaxiDriverEntityNotFoundException::new);
    }

    public TaxiDriverStatus getCurrentTaxiDriverStatus(String taxiDriverLoginId) {

        String key = RedisKeyParser.taxiDriverStatus(taxiDriverLoginId);

        // 1.Redis 조회
        String status = redisService.getValue(key);

        if (status != null) {
            return TaxiDriverStatus.valueOf(status);
        }

        // 2.없으면 DB 에서 조회.
        TaxiDriverStatus dbStatus = taxiDriverRepository.findByLoginId(taxiDriverLoginId)
                .orElseThrow(TaxiDriverEntityNotFoundException::new)
                .getStatus();

        // 3.이후 Redis 에 캐싱
        redisService.setValueWithTTL(key, dbStatus.name(), Duration.ofHours(1));

        return dbStatus;
    }

    public TaxiType getCurrentTaxiType(String taxiDriverLoginId) {
        String key = RedisKeyParser.taxiDriversTaxiType(taxiDriverLoginId);

        // 1. redis 조회
        String stringTaxiType = redisService.getValue(key);

        if (stringTaxiType != null) {
            return TaxiType.valueOf(stringTaxiType);
        }

        // 2. 없으면 DB 에서 조회
        TaxiTypeResponse taxiTypeResponse = taxiDriverRepository.findTaxiDriversTaxiTypeByLoginId(taxiDriverLoginId)
                .orElseThrow(TaxiDriverEntityNotFoundException::new);

        TaxiType taxiType = taxiTypeResponse.getTaxiType();

        // 3. 이후 redis 에 캐싱
        redisService.setValueWithTTL(key, taxiType.name(), Duration.ofDays(1));

        return taxiType;

    }

    public Long findIdByLoginId(String taxiDriverLoginId) {
        return taxiDriverRepository.findIdByLoginId(taxiDriverLoginId)
                .orElseThrow(TaxiDriverEntityNotFoundException::new);
    }
}
