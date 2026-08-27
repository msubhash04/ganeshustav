package com.ganeshutsav.backend.security;

import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.CommitteeRole;
import com.ganeshutsav.backend.entity.InspectionMode;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.CommitteeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Central helper for multi-tenant enforcement. Every domain service
 * (Donations, Expenses, Auctions, Loans, Sponsorships, FestivalYears)
 * calls this to figure out "who is calling, and which committee do they
 * belong to" - and every read/write in those services is filtered or
 * checked against that committee.
 *
 * SECURITY PRINCIPLE: the tenant is ALWAYS derived either from the
 * authenticated Member loaded fresh from the database, or - during a
 * Tenant Inspection session - from a signed JWT claim minted only by
 * DeveloperInspectionController. Never from a client-supplied
 * committeeId/tenantCode in the request body or URL. This is what makes
 * cross-tenant spoofing impossible: a malicious or buggy client simply
 * has no field it could tamper with to read another committee's data.
 */
@Component
@RequiredArgsConstructor
public class TenantContext {

    private final CommitteeRepository committeeRepository;

    /** The currently authenticated Member, or null if unauthenticated. */
    public Member getCurrentMember() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
        return (principal instanceof Member) ? (Member) principal : null;
    }

    public boolean isDeveloper() {
        Member m = getCurrentMember();
        return m != null && m.getRole() == CommitteeRole.DEVELOPER;
    }

    /** Inspection details for the current request, or an "empty" instance if not inspecting. */
    public InspectionDetails getInspectionDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object details = auth != null ? auth.getDetails() : null;
        return (details instanceof InspectionDetails) ? (InspectionDetails) details : InspectionDetails.none();
    }

    public boolean isInspecting() {
        return getInspectionDetails().isInspecting();
    }

    public InspectionMode getInspectionMode() {
        return getInspectionDetails().getMode();
    }

    /**
     * The current caller's committee. Normally the authenticated Member's
     * own committee. During a Tenant Inspection session, resolves instead
     * to the committee named in the (signed, server-issued) inspection
     * token - re-loaded fresh from the database on every call so a
     * committee locked mid-session is caught immediately. Throws if
     * neither applies (i.e. a DEVELOPER account with no active
     * inspection) - domain modules like Donations, Expenses, etc. all
     * require a tenant context to operate, by design. DEVELOPER manages
     * committees themselves via CommitteeController, not through these
     * tenant-scoped domain endpoints.
     */
    public Committee requireCommittee() {
        Member m = getCurrentMember();
        if (m == null) {
            throw new AccessDeniedException("Not authenticated.");
        }

        InspectionDetails inspection = getInspectionDetails();
        if (inspection.isInspecting()) {
            Committee committee = committeeRepository.findById(inspection.getInspectedCommitteeId())
                    .orElseThrow(() -> new EntityNotFoundException("The inspected committee no longer exists"));
            if (!committee.isActive()) {
                throw new AccessDeniedException("This committee has been locked - exit inspection mode.");
            }
            return committee;
        }

        if (m.getCommittee() == null) {
            throw new AccessDeniedException(
                    "This action requires a committee-scoped account. Developer (Super Admin) accounts " +
                    "manage committees via the Developer Dashboard, not this endpoint.");
        }
        return m.getCommittee();
    }

    public Long requireCommitteeId() {
        return requireCommittee().getId();
    }

    /**
     * Verifies an already-loaded entity's committee matches the caller's
     * own committee before allowing an update/delete. This is the actual
     * "President cannot modify another committee's data" guarantee -
     * call this at the top of every update()/delete() in every
     * tenant-scoped service, right after loading the entity by id.
     */
    public void assertOwnedByCurrentTenant(Committee entityCommittee) {
        Long myCommitteeId = requireCommitteeId();
        if (entityCommittee == null || !myCommitteeId.equals(entityCommittee.getId())) {
            throw new AccessDeniedException("You do not have access to this record - it belongs to a different committee.");
        }
    }
}
