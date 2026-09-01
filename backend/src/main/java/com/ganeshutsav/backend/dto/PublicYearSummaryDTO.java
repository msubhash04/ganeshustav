package com.ganeshutsav.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Public, unauthenticated, aggregate-only view of a single festival
 * year - active or archived. Deliberately carries NO donor names, phone
 * numbers, receipt numbers, winner names, or any other itemized ledger
 * row; only totals and a category breakdown, same privacy guarantee as
 * the existing /api/public/transparency/{tenantCode} endpoint.
 */
public class PublicYearSummaryDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String committeeName;
        private String tenantCode;
        // false only from the "active year" lookup, meaning this
        // committee currently has no live festival for the current
        // calendar year - every other field is then omitted/null
        private boolean found;
        private Long festivalYearId;
        private String label;
        private Integer year;
        private LocalDate startDate;
        private Integer durationDays;
        private boolean active;
        private BigDecimal totalCollections;
        private BigDecimal totalExpenses;
        private BigDecimal totalSponsorships;
        private BigDecimal totalAuctionEarnings;
        private BigDecimal netSurplusOrDeficit;
        private Map<String, BigDecimal> expenseByCategory;
        private long totalDonorCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class YearOption {
        private Long id;
        private String label;
        private Integer year;
        private boolean active;
    }
}
