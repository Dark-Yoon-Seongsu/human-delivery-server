package goorm.humandelivery.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DrivingInfoEntityNotFoundException extends RuntimeException {

    public DrivingInfoEntityNotFoundException() {
        super(
                "해당하는 아이디를 가진 DrivingInfo 엔티티가 존재하지 않습니다."
        );

    }
}
