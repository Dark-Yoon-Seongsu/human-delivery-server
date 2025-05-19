package goorm.humandelivery.global.exception;

public class TaxiDriverEntityNotFoundException extends RuntimeException {

    public TaxiDriverEntityNotFoundException() {
        super("아이디에 해당하는 TaxiDriver 엔티티가 존재하지 않습니다.");
    }
}
