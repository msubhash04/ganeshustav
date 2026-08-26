package com.ganeshutsav.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Represents both a committee member and a login account.
 * Only committee members can log in and add/edit transactions.
 *
 * MULTI-TENANCY: `committee` is nullable. It is NULL only for DEVELOPER
 * (Super Admin) accounts, which are global and not scoped to any single
 * committee. Every other role (PRESIDENT, TREASURER, SECRETARY,
 * VOLUNTEER) must have a committee assigned - enforced in the service
 * layer, not the database, so the schema stays simple.
 */
@Entity
@Table(name = "members")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommitteeRole role;

    // Login credentials - username is globally unique across the whole
    // platform (login does not require selecting a tenant first)
    @Column(nullable = false, unique = true)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password; // BCrypt hashed

    @Column(nullable = false)
    private boolean active;

    // NULL only for DEVELOPER accounts - every other role belongs to exactly one committee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_id")
    @ToString.Exclude
    private Committee committee;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.active == false) {
            this.active = true;
        }
    }
}
