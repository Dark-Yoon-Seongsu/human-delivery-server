package goorm.humandelivery.global.exception;

public class OffDutyLocationUpdateException extends RuntimeException {
    public OffDutyLocationUpdateException() {
        super("미운행 상태에서는 위치정보를 전송할 수 없습니다.");
    }
}
