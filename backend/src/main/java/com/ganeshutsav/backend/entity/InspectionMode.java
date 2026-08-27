package com.ganeshutsav.backend.entity;

public enum InspectionMode {
    // Developer can view every screen a President sees, but every
    // non-GET request is blocked at InspectionModeFilter before it
    // reaches any controller - no exceptions, regardless of role checks
    // on the individual endpoint.
    READ_ONLY,

    // Developer is additionally granted ROLE_PRESIDENT for the duration
    // of the inspection session (see JwtAuthFilter), so they can create/
    // edit/delete on the inspected committee's behalf. Every mutating
    // request in this mode is written to inspection_audit_logs.
    ADMIN_OVERRIDE
}
