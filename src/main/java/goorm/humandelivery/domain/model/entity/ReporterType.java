package goorm.humandelivery.domain.model.entity;

public enum ReporterType {
    CUSTOMER("고객"),
    TAXI_DRIVER("택시기사");

    private final String description;

    ReporterType(String description) {
        this.description = description;
    }
}
