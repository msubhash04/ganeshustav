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
        @NotBlank
        private String role; // PRESIDENT / TREASURER / SECRETARY / VOLUNTEER
    }
}
