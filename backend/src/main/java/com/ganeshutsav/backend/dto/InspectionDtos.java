package com.ganeshutsav.backend.dto;

import com.ganeshutsav.backend.entity.InspectionMode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class InspectionDtos {

    @Data
    public static class StartInspectionRequest {
        @NotNull(message = "mode is required (READ_ONLY or ADMIN_OVERRIDE)")
        private InspectionMode mode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InspectionTokenResponse {
        private String inspectionToken;
        private long expiresInMs;
        private Long committeeId;
        private String committeeName;
        private String tenantCode;
        private InspectionMode mode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InspectionAuditEntry {
        private Long id;
        private String developerUsername;
        private Long committeeId;
        private String tenantCode;
        private InspectionMode mode;
        private String eventType;
        private String httpMethod;
        private String path;
        private LocalDateTime occurredAt;
    }
}
