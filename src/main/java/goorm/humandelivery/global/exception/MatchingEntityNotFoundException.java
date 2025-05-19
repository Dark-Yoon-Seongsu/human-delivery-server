package goorm.humandelivery.global.exception;

public class MatchingEntityNotFoundException extends RuntimeException {

    public MatchingEntityNotFoundException() {
        super("해당 아이디를 가진 Matching 엔티티가 존재하지 않습니다.");
    }
}
