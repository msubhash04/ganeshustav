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
 * Post-festival micro-loan given out of leftover funds. Interest accrues
 * monthly on the REDUCING balance (see LoanService for the calculation).
 * currentPrincipal starts equal to originalPrincipal and decreases as
 * repayments are applied; lastInterestDate marks the last point interest
 * was calculated up to (either loan start, or the last repayment date).
 */
@Entity
@Table(name = "loans")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_year_id")
    @ToString.Exclude
    private FestivalYear festivalYear; // year the surplus originated from

    @Column(nullable = false)
    private String borrowerName;

    @Column(length = 15)
    private String borrowerPhone;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrincipal;

    // remaining principal after all repayments so far (reducing balance)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrincipal;

    // monthly interest rate as a percentage, e.g. 2.00 means 2% per month
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal monthlyInterestRatePercent;

    @Column(nullable = false)
    private LocalDate loanDate;

    // last date interest was calculated up to (loan start, or last repayment)
    @Column(nullable = false)
    private LocalDate lastInterestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

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
        if (this.status == null) this.status = LoanStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
