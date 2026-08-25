package com.ganeshutsav.backend.dto;

import com.ganeshutsav.backend.entity.PaymentMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DonationDTO {
    private Long id;

    @NotBlank(message = "Donor name is required")
    private String donorName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String address;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    @NotNull(message = "Date is required")
    private LocalDate donationDate;

    // auto-generated if left blank
    private String receiptNumber;

    private String recordedByName;

    // which festival year this donation belongs to (defaults to the active year if omitted)
    private Long festivalYearId;
}
