package goorm.humandelivery.domain.model.entity;

public enum ReportStatus {
    PROCESSING("처리중"),
    COMPLETED("처리완료"),
    ;

    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }
}
