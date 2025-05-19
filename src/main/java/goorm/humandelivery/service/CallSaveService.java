package goorm.humandelivery.service;

import goorm.humandelivery.domain.model.entity.CallInfo;
import goorm.humandelivery.domain.repository.CallInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CallSaveService {

    private final CallInfoRepository callInfoRepository;

    public CallSaveService(CallInfoRepository callInfoRepository) {
        this.callInfoRepository = callInfoRepository;
    }

    public CallInfo saveCallInfo(CallInfo callInfo) {
        validateCallInfo(callInfo);
        return callInfoRepository.save(callInfo);
    }

    private void validateCallInfo(CallInfo callInfo) {
        if (callInfo.getCustomer() == null) {
            throw new IllegalArgumentException("고객 정보는 필수입니다.");
        }
        if (callInfo.getExpectedOrigin() == null) {
            throw new IllegalArgumentException("출발 위치 정보는 필수입니다.");
        }
        if (callInfo.getExpectedDestination() == null) {
            throw new IllegalArgumentException("도착 위치 정보는 필수입니다.");
        }

        validateLocation(callInfo.getExpectedOrigin(), "출발");
        validateLocation(callInfo.getExpectedDestination(), "도착");
    }

    private void validateLocation(goorm.humandelivery.domain.model.entity.Location location, String type) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        // 대한민국 대략적인 위경도 범위
        boolean validLat = lat >= 33.0 && lat <= 43.0;
        boolean validLon = lon >= 124.0 && lon <= 131.0;

        if (!validLat || !validLon) {
            throw new IllegalArgumentException(type + " 위치 정보가 대한민국 범위를 벗어났습니다.");
        }
    }
}
