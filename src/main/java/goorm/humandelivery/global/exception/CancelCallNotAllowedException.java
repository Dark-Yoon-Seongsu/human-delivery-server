package goorm.humandelivery.global.exception;

public class CancelCallNotAllowedException extends RuntimeException {
  public CancelCallNotAllowedException(String message) {
    super(message);
  }
}