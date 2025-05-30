package goorm.humandelivery.global.exception;

public class JwtTokenGenerationException extends RuntimeException {
    public JwtTokenGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
