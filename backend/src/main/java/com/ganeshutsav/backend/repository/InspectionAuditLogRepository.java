package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.InspectionAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionAuditLogRepository extends JpaRepository<InspectionAuditLog, Long> {

    List<InspectionAuditLog> findByCommitteeIdOrderByOccurredAtDesc(Long committeeId);

    List<InspectionAuditLog> findByDeveloperIdOrderByOccurredAtDesc(Long developerId);

    List<InspectionAuditLog> findTop200ByOrderByOccurredAtDesc();
}
