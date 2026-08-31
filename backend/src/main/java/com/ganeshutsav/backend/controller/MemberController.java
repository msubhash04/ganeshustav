package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.MemberDtos.CreateStaffRequest;
import com.ganeshutsav.backend.dto.MemberDtos.MemberResponse;
import com.ganeshutsav.backend.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Committee staff management - scoped strictly to the caller's own
 * committee (via TenantContext, inside MemberService). Previously this
 * endpoint had no auth requirement at all and returned every member
 * across every committee; that has been fixed as part of the
 * multi-tenancy retrofit.
 *
 * RBAC is deliberately split per-method rather than at the class level:
 * VIEWING the roster is allowed for the President or a Developer who is
 * currently inspecting this committee (Tenant Inspection Mode) - so a
 * Developer can observe who's on a committee's staff while auditing it.
 * Every MUTATION (add/deactivate/remove staff) stays President-only,
 * and InspectionModeFilter additionally blocks all three even in Admin
 * Override mode - staff management stays off-limits during inspection
 * regardless of role authorities, by design.
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PRESIDENT','DEVELOPER')")
    public List<MemberResponse> getAll() {
        return memberService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    public MemberResponse create(@Valid @RequestBody CreateStaffRequest req) {
        return memberService.createStaff(req);
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('PRESIDENT')")
    public MemberResponse deactivate(@PathVariable Long id) {
        return memberService.deactivate(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public void delete(@PathVariable Long id) {
        memberService.delete(id);
    }
}
