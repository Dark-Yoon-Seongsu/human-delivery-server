package goorm.humandelivery.global.exception;

public class CustomerNotAssignedException extends RuntimeException {
    public CustomerNotAssignedException() {
        super("위치를 전송할 고객이 존재하지 않습니다.");
    }
}
