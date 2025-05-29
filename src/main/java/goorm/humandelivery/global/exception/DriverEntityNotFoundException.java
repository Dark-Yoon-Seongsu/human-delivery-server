package goorm.humandelivery.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DriverEntityNotFoundException extends RuntimeException {

    public DriverEntityNotFoundException() {
        super("아이디에 해당하는 엔티티가 존재하지 않습니다.");
    }

}
