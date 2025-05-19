package goorm.humandelivery.driver.domain;

public enum TaxiDriverStatus {
    OFF_DUTY("미운행"),
    AVAILABLE("빈차"),
    RESERVED("예약"),
    ON_DRIVING("배달중");

    private final String description;

    TaxiDriverStatus(String description) {
        this.description = description;
    }
}
