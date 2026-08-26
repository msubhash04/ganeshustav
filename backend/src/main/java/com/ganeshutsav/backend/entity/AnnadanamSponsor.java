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
 * A dedicated table for Annadanam (food distribution) sponsors, kept
 * separate from GeneralSponsor because Annadanam is a major daily
 * activity that needs to be tracked per festival day - i.e. "who is
 * sponsoring the meals on Day 3", not just a generic sponsor list.
 */
@Entity
@Table(name = "annadanam_sponsors")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnadanamSponsor {

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

    @Column(nullable = false)
    private String sponsorName;

    @Column(length = 15)
    private String contactInfo;

    // which festival day this sponsor is covering meals for (Day 1, Day 2, ...)
    @Column(nullable = false)
    private Integer dayNumber;

    // e.g. "Breakfast", "Lunch", "Dinner", "All Day" - kept as free text
    // rather than a fixed enum since committees vary in how they split meals
    private String mealSlot;

    @Column(precision = 12, scale = 2)
    private BigDecimal contributionAmount;

    @Column(length = 500)
    private String contributionDetails;

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
