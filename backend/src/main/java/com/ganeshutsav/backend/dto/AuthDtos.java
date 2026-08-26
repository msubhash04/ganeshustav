package com.ganeshutsav.backend.dto;

import com.ganeshutsav.backend.entity.CommitteeRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDtos {

    @Data
    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginResponse {
        private String token;
        private Long memberId;
        private String name;
        private String username;
        private CommitteeRole role;

        // null for DEVELOPER (Super Admin) accounts, which belong to no committee
        private Long committeeId;
        private String committeeName;
        private String tenantCode; // "Ganesh Unique Code"
    }

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String phone;
        private String email;
        @NotBlank
        private String username;
        @NotBlank
        private String password;

        // TREASURER / SECRETARY / VOLUNTEER only - PRESIDENT accounts are
        // created exclusively by the Developer when the committee itself
        // is created, and DEVELOPER accounts are never created via this
        // public endpoint at all
        @NotBlank
        private String role;

        // "Ganesh Unique Code" of the committee this member is joining -
        // required, since every non-Developer account must belong to
        // exactly one committee
        @NotBlank
        private String tenantCode;
    }
}
