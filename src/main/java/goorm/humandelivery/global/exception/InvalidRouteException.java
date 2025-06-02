package goorm.humandelivery.global.exception;

import lombok.Getter;

@Getter
public class InvalidRouteException extends RuntimeException {
    private final int resultCode;

    public InvalidRouteException(int resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public InvalidRouteException(int resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
    }
}
