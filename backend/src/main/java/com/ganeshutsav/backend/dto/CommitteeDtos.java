package com.ganeshutsav.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class CommitteeDtos {

    // Developer-only: creates a committee AND its first President in one call
    @Data
    public static class CreateCommitteeRequest {
        @NotBlank(message = "Committee name is required")
        private String name;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "State is required")
        private String state;

        private String address;

        // initial President credentials
        @NotBlank(message = "President name is required")
        private String presidentName;

        @NotBlank(message = "President phone is required")
        private String presidentPhone;

        @NotBlank(message = "President username is required")
        private String presidentUsername;

        @NotBlank(message = "President password is required")
        private String presidentPassword;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommitteeResponse {
        private Long id;
        private String tenantCode;
        private String name;
        private String city;
        private String state;
        private String address;
        private boolean active;
        private LocalDateTime createdAt;
        // populated only on the Developer's Committee Directory / detail view
        private Long memberCount;
    }

    @Data
    public static class UpdateCommitteeRequest {
        @NotBlank(message = "Committee name is required")
        private String name;
        private String city;
        private String state;
        private String address;
    }
}
