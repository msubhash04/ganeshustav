package com.ganeshutsav.backend.security;

import com.ganeshutsav.backend.entity.InspectionMode;

/**
 * Attached to the Spring Security Authentication's "details" field by
 * JwtAuthFilter whenever the presented JWT is a Tenant Inspection token
 * (see JwtUtil.generateInspectionToken). Non-inspection requests get an
 * instance with everything null, so TenantContext and
 * InspectionModeFilter never have to null-check the Authentication itself
 * - only the fields on this object.
 *
 * Deliberately NOT used to change the authenticated principal (still the
 * real Developer's Member row) or the "role" claim (still DEVELOPER) -
 * only ROLE_PRESIDENT is additionally granted as an authority in
 * ADMIN_OVERRIDE mode. This keeps every audit trail and every
 * SecurityContextHolder.getAuthentication().getName() call truthful about
 * who is actually acting.
 */
public class InspectionDetails {

    private final Long inspectedCommitteeId;
    private final String inspectedTenantCode;
    private final InspectionMode mode;

    public InspectionDetails(Long inspectedCommitteeId, String inspectedTenantCode, InspectionMode mode) {
        this.inspectedCommitteeId = inspectedCommitteeId;
        this.inspectedTenantCode = inspectedTenantCode;
        this.mode = mode;
    }

    public static InspectionDetails none() {
        return new InspectionDetails(null, null, null);
    }

    public boolean isInspecting() {
        return inspectedCommitteeId != null;
    }

    public Long getInspectedCommitteeId() {
        return inspectedCommitteeId;
    }

    public String getInspectedTenantCode() {
        return inspectedTenantCode;
    }

    public InspectionMode getMode() {
        return mode;
    }
}
