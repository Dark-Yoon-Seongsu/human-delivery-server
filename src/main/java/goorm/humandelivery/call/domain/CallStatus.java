package goorm.humandelivery.call.domain;

public enum CallStatus {
    SENT("배차요청"),
    MATCHED("배차"),
    CANCELLED("취소"),
    COMPLETED("배달완료");

    private final String description;

    CallStatus(String description) {
        this.description = description;
    }
}
