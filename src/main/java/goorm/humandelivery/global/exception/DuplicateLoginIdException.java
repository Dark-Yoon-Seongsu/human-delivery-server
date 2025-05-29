package goorm.humandelivery.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateLoginIdException extends RuntimeException {
    public DuplicateLoginIdException() {
        super("이미 사용 중인 아이디입니다.");
    }

}
