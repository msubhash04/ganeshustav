package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.PublicYearSummaryDTO.Response;
import com.ganeshutsav.backend.dto.PublicYearSummaryDTO.YearOption;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.repository.CommitteeRepository;
import com.ganeshutsav.backend.repository.DonationRepository;
import com.ganeshutsav.backend.repository.ExpenseRepository;
import com.ganeshutsav.backend.service.PublicService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Public, read-only, unauthenticated endpoints - no JWT, no committee
 * membership required. Everything here is addressed by a committee's
 * Ganesh Unique Code (tenantCode) from the URL, so one committee's page
 * can never leak into another's, and every response intentionally
 * excludes donor names, phone numbers, winner names or any other
 * itemized/PII ledger row - aggregate totals only.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final CommitteeRepository committeeRepository;
    private final DonationRepository donationRepository;
    private final ExpenseRepository expenseRepository;
    private final PublicService publicService;

    // Pre-existing all-time transparency summary - left completely
    // untouched. New, festival-year-scoped endpoints for the landing
    // page's Public Committee Viewer and Read-Only Observation
    // Dashboard are below, in PublicService.
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

    // Used by the landing page's "Access Public Features" search box and
    // the Read-Only Observation Dashboard's live view. found=false (not
    // a 404) means the code is valid but there's no live festival right
    // now; a 404 (EntityNotFoundException, handled by
    // GlobalExceptionHandler) means the code itself doesn't exist.
    @GetMapping("/observe/{tenantCode}")
    public Response observeActiveFestival(@PathVariable String tenantCode) {
        return publicService.getActiveYearSummary(tenantCode);
    }

    // Historical Selector for "Past Festivals / Festival Archives" -
    // available to every visitor, no login required.
    @GetMapping("/committees/{tenantCode}/years")
    public List<YearOption> getYearOptions(@PathVariable String tenantCode) {
        return publicService.getYearOptions(tenantCode);
    }

    @GetMapping("/committees/{tenantCode}/years/{festivalYearId}/summary")
    public Response getYearSummary(@PathVariable String tenantCode, @PathVariable Long festivalYearId) {
        return publicService.getYearSummary(tenantCode, festivalYearId);
    }
}

