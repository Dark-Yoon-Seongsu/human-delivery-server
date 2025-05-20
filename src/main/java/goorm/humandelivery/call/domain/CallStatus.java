package goorm.humandelivery.call.domain;

public enum CallStatus {
    SENT("배차요청"),
    CANCELED("취소"),
    DONE("배차완료");

    private final String description;

    CallStatus(String description) {
        this.description = description;
    }
}
