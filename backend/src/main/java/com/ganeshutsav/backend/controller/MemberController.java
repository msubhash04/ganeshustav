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
 * Committee staff management - President-only, scoped strictly to their
 * own committee. Previously this endpoint had no auth requirement at all
 * and returned every member across every committee; that has been fixed
 * as part of the multi-tenancy retrofit.
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRESIDENT')")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public List<MemberResponse> getAll() {
        return memberService.getAll();
    }

    @PostMapping
    public MemberResponse create(@Valid @RequestBody CreateStaffRequest req) {
        return memberService.createStaff(req);
    }

    @PutMapping("/{id}/deactivate")
    public MemberResponse deactivate(@PathVariable Long id) {
        return memberService.deactivate(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        memberService.delete(id);
    }
}
