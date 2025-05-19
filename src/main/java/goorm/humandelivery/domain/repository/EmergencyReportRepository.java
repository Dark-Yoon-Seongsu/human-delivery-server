package goorm.humandelivery.domain.repository;

import goorm.humandelivery.domain.model.entity.EmergencyReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyReportRepository extends JpaRepository<EmergencyReport, Long> {
}
