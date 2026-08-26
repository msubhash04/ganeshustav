package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.CommitteeDtos.*;
import com.ganeshutsav.backend.service.CommitteeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Tenant Management - creating, listing, and administering Ganesh
 * Committees. Only the Developer (Super Admin) role can reach this
 * controller at all, per the module spec: "Only the Developer can
 * create new Ganesh Committees."
 */
@RestController
@RequestMapping("/api/committees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DEVELOPER')")
public class CommitteeController {

    private final CommitteeService committeeService;

    // Committee Directory - searchable, filterable by city/state
    @GetMapping
    public List<CommitteeResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state
    ) {
        return committeeService.search(query, city, state);
    }

    @GetMapping("/{id}")
    public CommitteeResponse getById(@PathVariable Long id) {
        return committeeService.getById(id);
    }

    // Create a new Ganesh Committee AND its initial President in one step -
    // generates the Ganesh Unique Code automatically
    @PostMapping
    public ResponseEntity<CommitteeResponse> create(@Valid @RequestBody CreateCommitteeRequest req) {
        return ResponseEntity.ok(committeeService.createCommitteeWithPresident(req));
    }

    @PutMapping("/{id}")
    public CommitteeResponse update(@PathVariable Long id, @Valid @RequestBody UpdateCommitteeRequest req) {
        return committeeService.update(id, req);
    }

    @PostMapping("/{id}/regenerate-code")
    public CommitteeResponse regenerateCode(@PathVariable Long id) {
        return committeeService.regenerateTenantCode(id);
    }

    @PutMapping("/{id}/lock")
    public CommitteeResponse lock(@PathVariable Long id) {
        return committeeService.setLocked(id, true);
    }

    @PutMapping("/{id}/unlock")
    public CommitteeResponse unlock(@PathVariable Long id) {
        return committeeService.setLocked(id, false);
    }
}
