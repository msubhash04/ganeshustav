package com.ganeshutsav.backend.dto;

import com.ganeshutsav.backend.entity.LoanStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class LoanDtos {

    @Data
    public static class LoanRequest {
        @NotBlank(message = "Borrower name is required")
        private String borrowerName;

        private String borrowerPhone;

        @NotNull(message = "Principal amount is required")
        @Positive(message = "Principal amount must be greater than zero")
        private BigDecimal principalAmount;

        @NotNull(message = "Monthly interest rate is required")
        @PositiveOrZero(message = "Interest rate cannot be negative")
        private BigDecimal monthlyInterestRatePercent;

        @NotNull(message = "Loan date is required")
        private LocalDate loanDate;

        private Long festivalYearId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoanResponse {
        private Long id;
        private String borrowerName;
        private String borrowerPhone;
        private BigDecimal originalPrincipal;
        private BigDecimal currentPrincipal;
        private BigDecimal monthlyInterestRatePercent;
        private LocalDate loanDate;
        private LocalDate lastInterestDate;
        private LoanStatus status;
        private BigDecimal accruedInterestAsOfToday; // computed, informational
        private List<RepaymentResponse> repayments;
    }

    @Data
    public static class RepaymentRequest {
        @NotNull(message = "Payment date is required")
        private LocalDate paymentDate;

        @NotNull(message = "Payment amount is required")
        @Positive(message = "Payment amount must be greater than zero")
        private BigDecimal paymentAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RepaymentResponse {
        private Long id;
        private LocalDate paymentDate;
        private BigDecimal paymentAmount;
        private BigDecimal interestPortion;
        private BigDecimal principalPortion;
        private BigDecimal remainingPrincipalAfter;
    }
}
