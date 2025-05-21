package goorm.humandelivery.driving.domain;

public enum DrivingStatus {
    ON_DRIVING("손님탑승완료"),
    COMPLETE("운행완료");

    private final String description;

    DrivingStatus(String description) {
        this.description = description;
    }
}
