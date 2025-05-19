package goorm.humandelivery.global.exception;

public class DrivingInfoEntityNotFoundException extends RuntimeException {

    public DrivingInfoEntityNotFoundException() {
        super(
                "해당하는 아이디를 가진 DrivingInfo 엔티티가 존재하지 않습니다."
        );

    }
}
