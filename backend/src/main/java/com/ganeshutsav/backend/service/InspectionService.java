package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.InspectionDtos.InspectionAuditEntry;
import com.ganeshutsav.backend.dto.InspectionDtos.InspectionTokenResponse;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.InspectionAuditLog;
import com.ganeshutsav.backend.entity.InspectionMode;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.CommitteeRepository;
import com.ganeshutsav.backend.repository.InspectionAuditLogRepository;
import com.ganeshutsav.backend.security.InspectionDetails;
import com.ganeshutsav.backend.security.JwtUtil;
import com.ganeshutsav.backend.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Tenant Inspection ("View as President") - lets a Developer temporarily
 * assume a committee's tenant context to view (READ_ONLY) or, when
 * explicitly needed, act on behalf of (ADMIN_OVERRIDE) that committee.
 * See TenantContext, InspectionModeFilter and JwtAuthFilter for how the
 * resulting token is actually enforced on every subsequent request.
 */
@Service
@RequiredArgsConstructor
public class InspectionService {

    private final CommitteeRepository committeeRepository;
    private final InspectionAuditLogRepository auditLogRepository;
    private final JwtUtil jwtUtil;
    private final TenantContext tenantContext;

    @Transactional
    public InspectionTokenResponse start(Long committeeId, InspectionMode mode) {
        Committee committee = committeeRepository.findById(committeeId)
                .orElseThrow(() -> new EntityNotFoundException("Committee not found: " + committeeId));
        if (!committee.isActive()) {
            throw new IllegalStateException("This committee is locked - unlock it before inspecting.");
        }

        Member developer = tenantContext.getCurrentMember();
        auditLogRepository.save(InspectionAuditLog.builder()
                .developerId(developer.getId())
                .developerUsername(developer.getUsername())
                .committeeId(committee.getId())
                .committeeTenantCode(committee.getTenantCode())
                .mode(mode)
                .eventType("SESSION_START")
                .build());

        String token = jwtUtil.generateInspectionToken(
                developer.getUsername(), committee.getId(), committee.getTenantCode(), mode);

        return InspectionTokenResponse.builder()
                .inspectionToken(token)
                .expiresInMs(jwtUtil.getInspectionExpirationMs())
                .committeeId(committee.getId())
                .committeeName(committee.getName())
                .tenantCode(committee.getTenantCode())
                .mode(mode)
                .build();
    }

    @Transactional
    public void exit() {
        InspectionDetails details = tenantContext.getInspectionDetails();
        if (!details.isInspecting()) {
            throw new AccessDeniedException("Not currently inspecting any committee.");
        }
        Member developer = tenantContext.getCurrentMember();
        auditLogRepository.save(InspectionAuditLog.builder()
                .developerId(developer.getId())
                .developerUsername(developer.getUsername())
                .committeeId(details.getInspectedCommitteeId())
                .committeeTenantCode(details.getInspectedTenantCode())
                .mode(details.getMode())
                .eventType("SESSION_END")
                .build());
    }

    // Developer-only, most-recent-first, capped to keep the response
    // small - the audit trail itself has no retention limit in the DB.
    public List<InspectionAuditEntry> getHistory() {
        return auditLogRepository.findTop200ByOrderByOccurredAtDesc().stream()
                .map(this::toEntry)
                .collect(Collectors.toList());
    }

    private InspectionAuditEntry toEntry(InspectionAuditLog log) {
        return InspectionAuditEntry.builder()
                .id(log.getId())
                .developerUsername(log.getDeveloperUsername())
                .committeeId(log.getCommitteeId())
                .tenantCode(log.getCommitteeTenantCode())
                .mode(log.getMode())
                .eventType(log.getEventType())
                .httpMethod(log.getHttpMethod())
                .path(log.getPath())
                .occurredAt(log.getOccurredAt())
                .build();
    }
}
