package goorm.humandelivery.global.exception;

public class AlreadyAssignedCallException extends RuntimeException {

    public AlreadyAssignedCallException() {
        super("이미 콜이 할당 완료된 택시입니다.");
    }
}
