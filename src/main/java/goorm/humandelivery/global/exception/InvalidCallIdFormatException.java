package goorm.humandelivery.global.exception;

public class InvalidCallIdFormatException extends RuntimeException {
    public InvalidCallIdFormatException(String callId) {
        super("올바르지 않은 콜 ID 형식입니다: " + callId);
    }
}
