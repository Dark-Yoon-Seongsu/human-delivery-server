package goorm.humandelivery.global.exception;

public class CallAlreadyCompletedException extends RuntimeException {

    public CallAlreadyCompletedException() {
        super("이미 완료된 배차 요청입니다.");
    }
}
