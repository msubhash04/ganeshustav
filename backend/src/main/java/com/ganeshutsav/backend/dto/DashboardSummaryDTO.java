package com.ganeshutsav.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {
    private BigDecimal totalCollection;         // collected this festival year only
    private BigDecimal totalExpenses;
    private BigDecimal balanceRemaining;
    private BigDecimal carryForwardBalance;      // leftover funds from previous year
    private BigDecimal grandTotalAvailableFunds; // carryForward + totalCollection - totalExpenses
    private String activeFestivalYearLabel;
    private Map<String, BigDecimal> expenseByCategory;       // pie chart
    private List<MonthlyTrendDTO> monthlyTrend;               // bar chart
    private List<RecentTransactionDTO> recentTransactions;    // last 5-10 combined

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyTrendDTO {
        private String period; // e.g. "2026-08"
        private BigDecimal collections;
        private BigDecimal expenses;
    }

    // plain-value snapshot of a Donation or Expense - never holds a live
    // entity reference, so it's always safe to serialize even after the
    // Hibernate session that built it has closed
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentTransactionDTO {
        private String type;        // "COLLECTION" or "EXPENSE"
        private String label;       // donor name, or expense description
        private java.time.LocalDate date;
        private BigDecimal amount;
    }
}
