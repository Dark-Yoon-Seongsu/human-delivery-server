package goorm.humandelivery.global.exception;

public class IncorrectTaxiDriverStatusException extends RuntimeException {

    public IncorrectTaxiDriverStatusException() {
        super("AVAILABLE 상태가 아닌 택시기사는 콜 요청을 수락할 수 없습니다.");
    }
}
