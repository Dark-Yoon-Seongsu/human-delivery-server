package goorm.humandelivery.global.exception;

public class LocationNotInRedisException extends RuntimeException {
    public LocationNotInRedisException(String key, String taxiDriverLoginId) {
        super("해당 로케이션 집합에 택시기사 위치가 존재하지 않습니다. key : " + key + ", taxi driver login id : " + taxiDriverLoginId);
    }
}
