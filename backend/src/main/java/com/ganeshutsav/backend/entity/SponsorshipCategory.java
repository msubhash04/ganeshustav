package com.ganeshutsav.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Master list of sponsorship categories (e.g. "Vigraha Dhata" / Idol Sponsor,
 * "Laddu Dhata" / Laddu Sponsor). Managed by the President; feeds the
 * category dropdown on the General Sponsors page.
 */
@Entity
@Table(name = "sponsorship_categories",
       uniqueConstraints = @UniqueConstraint(columnNames = {"committee_id", "name"}))
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorshipCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // MULTI-TENANCY: every row belongs to exactly one Ganesh Committee.
    // Always set server-side from the authenticated caller's own
    // committee - never trusted from client input - to guarantee one
    // committee can never read or write another committee's data.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_id", nullable = false)
    @ToString.Exclude
    private Committee committee;

    // unique PER COMMITTEE only (see the table-level constraint above)
    @Column(nullable = false)
    private String name; // e.g. "Vigraha Dhata (Idol Sponsor)"

    @Column(length = 500)
    private String description;

    // lets the President retire a category from the "add sponsor" dropdown
    // without deleting historical sponsors already assigned to it
    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    @ToString.Exclude
    private Member createdBy;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (!this.active) this.active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
