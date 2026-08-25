package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.repository.DonationRepository;
import com.ganeshutsav.backend.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public, read-only transparency endpoint. Intentionally excludes donor names,
 * phone numbers and addresses - only aggregate totals are exposed.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final DonationRepository donationRepository;
    private final ExpenseRepository expenseRepository;

    @GetMapping("/transparency")
    public Map<String, Object> getTransparencySummary() {
        BigDecimal totalCollection = donationRepository.getTotalCollection();
        BigDecimal totalExpenses = expenseRepository.getTotalExpenses();

        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        expenseRepository.getCategoryWiseTotals()
                .forEach(row -> categoryTotals.put(row.getCategory().getLabel(), row.getTotal()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCollection", totalCollection);
        result.put("totalExpenses", totalExpenses);
        result.put("balanceRemaining", totalCollection.subtract(totalExpenses));
        result.put("expenseByCategory", categoryTotals);
        result.put("totalDonors", donationRepository.count());
        return result;
    }
}
