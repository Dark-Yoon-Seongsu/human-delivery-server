package goorm.humandelivery.driver.domain;

public enum TaxiType {
    NORMAL("일반택시"),
    PREMIUM("모범택시"),
    VENTI("대형택시");

    private final String description;

    TaxiType(String description) {
        this.description = description;
    }
}
