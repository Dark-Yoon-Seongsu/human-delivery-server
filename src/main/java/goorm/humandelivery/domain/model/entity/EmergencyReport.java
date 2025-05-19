package goorm.humandelivery.domain.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmergencyReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emergency_report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driving_info_id")
    private DrivingInfo drivingInfo;

    @Enumerated(value = EnumType.STRING)
    private ReporterType reporterType;

    private String contents; // 신고 내용

    @Enumerated(value = EnumType.STRING)
    private ReportStatus reportStatus;
}
