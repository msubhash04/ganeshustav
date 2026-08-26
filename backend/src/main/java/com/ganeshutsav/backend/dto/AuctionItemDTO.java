package com.ganeshutsav.backend.dto;

import com.ganeshutsav.backend.entity.PaymentMode;
import com.ganeshutsav.backend.entity.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AuctionItemDTO {
    private Long id;

    private Integer dayNumber; // nullable - null = final day / not day-specific

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotBlank(message = "Winner's name is required")
    private String winnerName;

    @NotNull(message = "Bid amount is required")
    @Positive(message = "Bid amount must be greater than zero")
    private BigDecimal bidAmount;

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;

    private PaymentMode paymentMode; // required only when status = PAID

    private String recordedByName;
}
