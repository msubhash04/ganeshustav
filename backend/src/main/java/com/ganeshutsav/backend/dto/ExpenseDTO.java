package com.ganeshutsav.backend.dto;

import com.ganeshutsav.backend.entity.ExpenseCategory;
import com.ganeshutsav.backend.entity.PaymentMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseDTO {
    private Long id;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Paid To / Vendor name is required")
    private String paidTo;

    @NotNull(message = "Date is required")
    private LocalDate expenseDate;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    private String billFilePath;

    private String recordedByName;
    private String approvedByName;

    // which festival year this expense belongs to (defaults to the active year if omitted)
    private Long festivalYearId;

    // 1-based festival day (Day 1, Day 2, ...) - required for day-wise expense sheets
    private Integer dayNumber;

    // required when category is MISCELLANEOUS (Gift Distribution / Others) -
    // enforced in ExpenseService, since bean validation alone can't do
    // conditional-required-field checks cleanly
    private String note;
}
