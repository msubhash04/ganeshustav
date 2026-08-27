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

    // Public self-registration (RegisterRequest) has been removed - see the
    // comment in AuthController for why. Staff accounts are now created
    // exclusively via POST /api/members by an authenticated PRESIDENT.

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        private String newPassword;
    }
}
