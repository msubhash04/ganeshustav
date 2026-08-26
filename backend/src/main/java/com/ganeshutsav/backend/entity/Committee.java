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
 * The tenant root for multi-committee isolation. Every Ganesh Committee
 * registered on the platform is one row here.
 *
 * IMPORTANT DESIGN NOTE: all other tenant-scoped tables (FestivalYear,
 * Donation, Expense, AuctionItem, Loan, SponsorshipCategory,
 * GeneralSponsor, AnnadanamSponsor, Member) join back to this table via
 * the immutable surrogate primary key `id` - NOT via `tenantCode`.
 * `tenantCode` ("Ganesh Unique Code") is a separate, human-facing
 * business identifier that the Developer can regenerate at any time
 * without breaking a single foreign key relationship anywhere in the
 * system, because nothing else stores or joins on it directly.
 */
@Entity
@Table(name = "committees")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Committee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // "Ganesh Unique Code" - human-facing, regenerable, globally unique
    @Column(name = "tenant_code", nullable = false, unique = true, length = 20)
    private String tenantCode;

    @Column(nullable = false)
    private String name; // e.g. "Shivaji Nagar Ganesh Mandal"

    private String city;
    private String state;

    @Column(length = 500)
    private String address;

    // lets the Developer lock a committee's access post-festival without
    // deleting its historical data
    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_developer_id")
    @ToString.Exclude
    private Member createdByDeveloper;

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
