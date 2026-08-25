package com.ganeshutsav.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A general sponsor (distinct from Annadanam sponsors, which get their
 * own dedicated table/page since food distribution is tracked day-wise).
 * Each sponsor is assigned exactly one SponsorshipCategory, chosen from
 * the categories the President has set up in the master page.
 */
@Entity
@Table(name = "general_sponsors")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralSponsor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String sponsorName;

    @Column(length = 15)
    private String contactInfo; // phone or email

    // monetary value of the sponsorship, if applicable (nullable - some
    // sponsorships are in-kind, e.g. donated materials rather than cash)
    @Column(precision = 12, scale = 2)
    private BigDecimal contributionAmount;

    // free-text description of what was sponsored / how, e.g.
    // "Sponsored idol decoration flowers and garlands for all 5 days"
    @Column(length = 500)
    private String contributionDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    private SponsorshipCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_year_id")
    @ToString.Exclude
    private FestivalYear festivalYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id")
    @ToString.Exclude
    private Member recordedBy;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
