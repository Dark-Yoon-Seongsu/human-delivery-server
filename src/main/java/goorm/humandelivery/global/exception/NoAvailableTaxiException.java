package goorm.humandelivery.global.exception;

public class NoAvailableTaxiException extends RuntimeException {

    public NoAvailableTaxiException() {
        super("탐색 범위 내에 유효한 택시가 없습니다.");
    }
}
