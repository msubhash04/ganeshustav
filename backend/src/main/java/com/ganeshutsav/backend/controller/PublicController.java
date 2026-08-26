package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.repository.CommitteeRepository;
import com.ganeshutsav.backend.repository.DonationRepository;
import com.ganeshutsav.backend.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public, read-only transparency endpoint - one per committee, addressed
 * by its Ganesh Unique Code (tenantCode) in the URL. Intentionally
 * excludes donor names, phone numbers and addresses - only aggregate
 * totals for THAT ONE committee are exposed, never a cross-tenant view.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final CommitteeRepository committeeRepository;
    private final DonationRepository donationRepository;
    private final ExpenseRepository expenseRepository;

    @GetMapping("/transparency/{tenantCode}")
    public Map<String, Object> getTransparencySummary(@PathVariable String tenantCode) {
        Committee committee = committeeRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new EntityNotFoundException("No committee found with code: " + tenantCode));

        Long committeeId = committee.getId();
        BigDecimal totalCollection = donationRepository.getTotalCollection(committeeId);
        BigDecimal totalExpenses = expenseRepository.getTotalExpenses(committeeId);

        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        expenseRepository.getCategoryWiseTotals(committeeId)
                .forEach(row -> categoryTotals.put(row.getCategory().getLabel(), row.getTotal()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("committeeName", committee.getName());
        result.put("tenantCode", committee.getTenantCode());
        result.put("totalCollection", totalCollection);
        result.put("totalExpenses", totalExpenses);
        result.put("balanceRemaining", totalCollection.subtract(totalExpenses));
        result.put("expenseByCategory", categoryTotals);
        result.put("totalDonors", donationRepository.countByCommitteeId(committeeId));
        return result;
    }
}
