package com.ganeshutsav.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Public, unauthenticated, aggregate-only view of a single festival
 * year - active or archived. Deliberately carries NO donor names, phone
 * numbers, receipt numbers, winner names, or any other itemized ledger
 * row; only totals and a category breakdown, same privacy guarantee as
 * the existing /api/public/transparency/{tenantCode} endpoint.
 *
 * EXCEPTION: general and Annadanam sponsors ARE named here, deliberately
 * - unlike anonymous donors, sponsors are named partners a committee
 * publicly credits (a physical "sponsor board" at the pandal would show
 * the same names), so listing sponsorName + what they sponsored is
 * expected, not a privacy leak. Their contactInfo (phone/email) is still
 * never included.
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
        private List<GeneralSponsor> generalSponsors;
        private List<AnnadanamSponsor> annadanamSponsors;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeneralSponsor {
        private String sponsorName;
        private String categoryName;
        private BigDecimal contributionAmount;
        private String contributionDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnnadanamSponsor {
        private String sponsorName;
        private Integer dayNumber;
        private String mealSlot;
        private BigDecimal contributionAmount;
        private String contributionDetails;
    }
}
