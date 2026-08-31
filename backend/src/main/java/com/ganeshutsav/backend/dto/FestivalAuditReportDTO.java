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
 * Comprehensive, read-only report for ONE festival year - active or
 * archived - used by the "Festival Archives" section (accessible to
 * every committee role, not just the President) so anyone can audit a
 * past year's complete financial picture: summary, category breakdown,
 * and the full itemized ledger.
 */
public class FestivalAuditReportDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        // Which year this report covers
        private Long festivalYearId;
        private String label;
        private Integer year;
        private LocalDate startDate;
        private Integer durationDays;
        private boolean active; // false = archived

        // Financial summary
        private BigDecimal carryForwardBalance;
        private BigDecimal totalCollections;
        private BigDecimal totalExpenses;
        private BigDecimal totalSponsorships; // general + Annadanam combined
        private BigDecimal totalAuctionEarnings;
        private BigDecimal netSurplusOrDeficit; // carryForward + collections + sponsorships + auction - expenses

        // Category breakdown
        private Map<String, BigDecimal> expenseByCategory;
        private BigDecimal generalSponsorshipTotal;
        private BigDecimal annadanamSponsorshipTotal;

        // Full audit trail / ledger
        private List<LedgerDonationDTO> donations;
        private List<LedgerExpenseDTO> expenses;
        private List<LedgerAuctionItemDTO> auctionItems;
        private List<LedgerGeneralSponsorDTO> generalSponsors;
        private List<LedgerAnnadanamSponsorDTO> annadanamSponsors;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LedgerDonationDTO {
        private String receiptNumber;
        private String donorName;
        private String phoneNumber;
        private BigDecimal amount;
        private String paymentMode;
        private LocalDate donationDate;
        private String recordedByName;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LedgerExpenseDTO {
        private String description;
        private String category;
        private BigDecimal amount;
        private String paidTo;
        private LocalDate expenseDate;
        private Integer dayNumber;
        private String recordedByName;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LedgerAuctionItemDTO {
        private Integer dayNumber;
        private String itemName;
        private String winnerName;
        private BigDecimal bidAmount;
        private String paymentStatus;
        private String recordedByName;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LedgerGeneralSponsorDTO {
        private String sponsorName;
        private String categoryName;
        private BigDecimal contributionAmount;
        private String contactInfo;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LedgerAnnadanamSponsorDTO {
        private String sponsorName;
        private Integer dayNumber;
        private String mealSlot;
        private BigDecimal contributionAmount;
        private String contactInfo;
    }
}
