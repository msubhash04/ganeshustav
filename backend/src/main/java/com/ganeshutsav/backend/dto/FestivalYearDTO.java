package com.ganeshutsav.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FestivalYearDTO {
    private Long id;

    @NotBlank(message = "Label is required")
    private String label;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Duration (days) is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;

    @NotNull(message = "Carry-forward balance is required (enter 0 if none)")
    private BigDecimal carryForwardBalance;

    private boolean active;
}
