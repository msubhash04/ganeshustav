package com.ganeshutsav.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

public class SponsorshipDtos {

    @Data
    public static class CategoryDTO {
        private Long id;

        @NotBlank(message = "Category name is required")
        private String name;

        private String description;
        private boolean active;
    }

    @Data
    public static class GeneralSponsorDTO {
        private Long id;

        @NotBlank(message = "Sponsor name is required")
        private String sponsorName;

        private String contactInfo;

        @PositiveOrZero(message = "Contribution amount cannot be negative")
        private BigDecimal contributionAmount;

        private String contributionDetails;

        @NotNull(message = "Sponsorship category is required")
        private Long categoryId;

        // populated in responses only
        private String categoryName;

        private Long festivalYearId;
        private String recordedByName;
    }

    @Data
    public static class AnnadanamSponsorDTO {
        private Long id;

        @NotBlank(message = "Sponsor name is required")
        private String sponsorName;

        private String contactInfo;

        @NotNull(message = "Day number is required")
        private Integer dayNumber;

        private String mealSlot;

        @PositiveOrZero(message = "Contribution amount cannot be negative")
        private BigDecimal contributionAmount;

        private String contributionDetails;

        private Long festivalYearId;
        private String recordedByName;
    }
}
