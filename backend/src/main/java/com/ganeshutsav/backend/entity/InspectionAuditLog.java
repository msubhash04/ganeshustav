package com.ganeshutsav.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Records every Tenant Inspection ("View as President") session and, for
 * ADMIN_OVERRIDE sessions, every mutating request the Developer made
 * against the inspected committee's data.
 *
 * Kept intentionally lightweight (a handful of short columns, no request
 * bodies) - each row is well under 200 bytes, so even heavy inspection
 * usage adds negligible storage compared to the donations/expenses/
 * uploaded-bill data this app already stores.
 */
@Entity
@Table(name = "inspection_audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long developerId;

    @Column(nullable = false)
    private String developerUsername;

    @Column(nullable = false)
    private Long committeeId;

    private String committeeTenantCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionMode mode;

    // SESSION_START | SESSION_END | ACTION (a mutating request made
    // while inspecting in ADMIN_OVERRIDE mode)
    @Column(nullable = false, length = 20)
    private String eventType;

    // populated only for eventType = ACTION
    @Column(length = 10)
    private String httpMethod;

    @Column(length = 300)
    private String path;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    protected void onCreate() {
        this.occurredAt = LocalDateTime.now();
    }
}
