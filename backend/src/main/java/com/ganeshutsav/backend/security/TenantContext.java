package com.ganeshutsav.backend.security;

import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.CommitteeRole;
import com.ganeshutsav.backend.entity.Member;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Central helper for multi-tenant enforcement. Every domain service
 * (Donations, Expenses, Auctions, Loans, Sponsorships, FestivalYears)
 * calls this to figure out "who is calling, and which committee do they
 * belong to" - and every read/write in those services is filtered or
 * checked against that committee.
 *
 * SECURITY PRINCIPLE: the tenant is ALWAYS derived from the authenticated
 * Member loaded fresh from the database (via the JWT username claim,
 * already resolved by JwtAuthFilter) - never from a client-supplied
 * committeeId/tenantCode in the request body or URL. This is what makes
 * cross-tenant spoofing impossible: a malicious or buggy client simply
 * has no field it could tamper with to read another committee's data.
 */
@Component
public class TenantContext {

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

    /**
     * The current caller's committee. Throws if the caller has no
     * committee (i.e. is a DEVELOPER) - domain modules like Donations,
     * Expenses, etc. all require a tenant context to operate, by design.
     * DEVELOPER manages committees themselves via CommitteeController,
     * not through these tenant-scoped domain endpoints.
     */
    public Committee requireCommittee() {
        Member m = getCurrentMember();
        if (m == null || m.getCommittee() == null) {
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
