package goorm.humandelivery.global.exception;

public class CallInfoEntityNotFoundException extends RuntimeException {

    public CallInfoEntityNotFoundException() {
        super("아이디에 해당하는 CallInfo 엔티티가 존재하지 않습니다.");

    }
}
