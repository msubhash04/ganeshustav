package com.ganeshutsav.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents one year's festival "instance" (e.g. "2026 Ganesh Utsav").
 * The President creates this to set the carry-forward balance from last
 * year, the start date, and how many days the festival runs — and can
 * edit the date/duration later if plans change.
 */
@Entity
@Table(name = "festival_years")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String label; // e.g. "2026 Ganesh Utsav"

    @Column(nullable = false)
    private Integer year; // e.g. 2026

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private Integer durationDays; // e.g. 3, 5, 9, 11

    // leftover funds brought forward from the previous festival year
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal carryForwardBalance;

    // only one festival year should be "active" at a time for data-entry defaults
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
        if (this.carryForwardBalance == null) this.carryForwardBalance = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
