package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.InspectionDtos.InspectionAuditEntry;
import com.ganeshutsav.backend.dto.InspectionDtos.InspectionTokenResponse;
import com.ganeshutsav.backend.dto.InspectionDtos.StartInspectionRequest;
import com.ganeshutsav.backend.service.InspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Tenant Inspection ("View as President") - lets the Developer drill into
 * an individual committee's full dashboard, in either READ_ONLY or
 * ADMIN_OVERRIDE mode. See InspectionService for the actual logic and
 * TenantContext / InspectionModeFilter / JwtAuthFilter for how the
 * resulting token is enforced on every subsequent request.
 *
 * Restricted to DEVELOPER at the class level - note that /exit is called
 * WHILE HOLDING the inspection token itself, which is fine because that
 * token's role claim is always still DEVELOPER (see JwtUtil), never
 * PRESIDENT - only an additional ROLE_PRESIDENT authority is granted in
 * ADMIN_OVERRIDE mode, so this check still passes correctly.
 */
@RestController
@RequestMapping("/api/developer/inspect")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DEVELOPER')")
public class DeveloperInspectionController {

    private final InspectionService inspectionService;

    @PostMapping("/{committeeId}")
    public ResponseEntity<InspectionTokenResponse> start(@PathVariable Long committeeId,
                                                           @Valid @RequestBody StartInspectionRequest request) {
        return ResponseEntity.ok(inspectionService.start(committeeId, request.getMode()));
    }

    @PostMapping("/exit")
    public ResponseEntity<?> exit() {
        inspectionService.exit();
        return ResponseEntity.ok("Inspection session ended");
    }

    @GetMapping("/history")
    public List<InspectionAuditEntry> history() {
        return inspectionService.getHistory();
    }
}
