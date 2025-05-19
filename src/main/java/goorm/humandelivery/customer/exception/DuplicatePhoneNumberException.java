package goorm.humandelivery.customer.exception;

public class DuplicatePhoneNumberException extends RuntimeException {

    public DuplicatePhoneNumberException() {
        super("이미 등록된 전화번호입니다.");
    }

}
