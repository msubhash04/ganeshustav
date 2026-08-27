package com.ganeshutsav.backend.dto;

import com.ganeshutsav.backend.entity.CommitteeRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MemberDtos {

    // President adding a staff member (Treasurer/Secretary/Volunteer) to
    // their OWN committee. Committee is never accepted from the client -
    // it's always the caller's own, via TenantContext.
    @Data
    public static class CreateStaffRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String phone;
        // optional - blank/empty values are normalized to NULL in
        // MemberService before saving, since the column is unique and
        // MySQL would otherwise reject a second blank email as a duplicate
        @Email(message = "Must be a valid email address")
        private String email;
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        @NotBlank
        private String role; // TREASURER / SECRETARY / VOLUNTEER only
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberResponse {
        private Long id;
        private String name;
        private String phone;
        private String email;
        private CommitteeRole role;
        private String username;
        private boolean active;
    }
}
