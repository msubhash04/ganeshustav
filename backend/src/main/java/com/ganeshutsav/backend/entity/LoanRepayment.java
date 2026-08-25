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
 * One repayment installment against a Loan. interestPortion and
 * principalPortion record exactly how the payment was split at the time
 * it was made (reducing-balance method), so the full repayment history
 * stays auditable even as the loan's currentPrincipal keeps changing.
 */
@Entity
@Table(name = "loan_repayments")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    @ToString.Exclude
    private Loan loan;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal paymentAmount;

    // how much of this payment went toward accrued interest
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal interestPortion;

    // how much of this payment went toward reducing the principal
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal principalPortion;

    // principal remaining immediately after this payment was applied
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal remainingPrincipalAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id")
    @ToString.Exclude
    private Member recordedBy;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
