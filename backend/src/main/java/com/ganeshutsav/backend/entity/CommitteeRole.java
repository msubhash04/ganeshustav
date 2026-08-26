package com.ganeshutsav.backend.entity;

public enum CommitteeRole {
    // Global Super Admin - platform owner/developer. Not tied to any
    // committee (committee = null on the Member row). Can create and
    // manage all Ganesh Committees, but does not directly operate on
    // any single committee's collections/expenses/etc.
    DEVELOPER,

    // Committee Admin - full CRUD within their own committee only
    PRESIDENT,

    // Committee Staff - operational access within their own committee only
    TREASURER,
    SECRETARY,
    VOLUNTEER
}
