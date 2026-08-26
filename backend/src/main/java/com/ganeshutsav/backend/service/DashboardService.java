package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.DashboardSummaryDTO;
import com.ganeshutsav.backend.entity.Donation;
import com.ganeshutsav.backend.entity.Expense;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.repository.DonationRepository;
import com.ganeshutsav.backend.repository.ExpenseRepository;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import com.ganeshutsav.backend.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Committee-scoped dashboard for President/Members. Every query here is
 * filtered to the caller's own committee via TenantContext - the Developer's
 * cross-committee aggregate view lives separately in DeveloperDashboardService,
 * so a single accidental method call can never mix the two.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final DonationRepository donationRepository;
    private final ExpenseRepository expenseRepository;
    private final FestivalYearRepository festivalYearRepository;
    private final TenantContext tenantContext;
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    public DashboardSummaryDTO getSummary() {
        Long committeeId = tenantContext.requireCommitteeId();

        BigDecimal totalCollection = donationRepository.getTotalCollection(committeeId);
        BigDecimal totalExpenses = expenseRepository.getTotalExpenses(committeeId);
        BigDecimal balance = totalCollection.subtract(totalExpenses);

        Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();
        expenseRepository.getCategoryWiseTotals(committeeId)
                .forEach(row -> expenseByCategory.put(row.getCategory().getLabel(), row.getTotal()));

        Map<String, BigDecimal> collectionsByMonth = new TreeMap<>();
        Map<String, BigDecimal> expensesByMonth = new TreeMap<>();

        for (Donation d : donationRepository.findByCommitteeIdOrderByDonationDateDesc(committeeId)) {
            String key = d.getDonationDate().format(MONTH_FMT);
            collectionsByMonth.merge(key, d.getAmount(), BigDecimal::add);
        }
        for (Expense e : expenseRepository.findByCommitteeIdOrderByExpenseDateDesc(committeeId)) {
            String key = e.getExpenseDate().format(MONTH_FMT);
            expensesByMonth.merge(key, e.getAmount(), BigDecimal::add);
        }

        java.util.Set<String> allMonths = new java.util.TreeSet<>();
        allMonths.addAll(collectionsByMonth.keySet());
        allMonths.addAll(expensesByMonth.keySet());

        List<DashboardSummaryDTO.MonthlyTrendDTO> monthlyTrend = new ArrayList<>();
        for (String month : allMonths) {
            monthlyTrend.add(DashboardSummaryDTO.MonthlyTrendDTO.builder()
                    .period(month)
                    .collections(collectionsByMonth.getOrDefault(month, BigDecimal.ZERO))
                    .expenses(expensesByMonth.getOrDefault(month, BigDecimal.ZERO))
                    .build());
        }

        // recent transactions: merge last donations + expenses, sort by createdAt desc, take 10.
        // Converted to plain-value DTOs HERE, inside the transaction, so the entities'
        // lazy fields never need to be touched again after this method returns.
        record Recent(java.time.LocalDateTime createdAt, DashboardSummaryDTO.RecentTransactionDTO dto) {}
        List<Recent> recent = new ArrayList<>();
        for (Donation d : donationRepository.findTop10ByCommitteeIdOrderByCreatedAtDesc(committeeId)) {
            recent.add(new Recent(d.getCreatedAt(), DashboardSummaryDTO.RecentTransactionDTO.builder()
                    .type("COLLECTION")
                    .label(d.getDonorName())
                    .date(d.getDonationDate())
                    .amount(d.getAmount())
                    .build()));
        }
        for (Expense e : expenseRepository.findTop10ByCommitteeIdOrderByCreatedAtDesc(committeeId)) {
            recent.add(new Recent(e.getCreatedAt(), DashboardSummaryDTO.RecentTransactionDTO.builder()
                    .type("EXPENSE")
                    .label(e.getDescription())
                    .date(e.getExpenseDate())
                    .amount(e.getAmount())
                    .build()));
        }
        recent.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
        List<DashboardSummaryDTO.RecentTransactionDTO> recentDTOs = recent.stream()
                .limit(10)
                .map(Recent::dto)
                .collect(java.util.stream.Collectors.toList());

        FestivalYear activeYear = festivalYearRepository.findFirstByCommitteeIdAndActiveTrueOrderByIdDesc(committeeId).orElse(null);
        BigDecimal carryForward = activeYear != null ? activeYear.getCarryForwardBalance() : BigDecimal.ZERO;
        BigDecimal grandTotal = carryForward.add(totalCollection).subtract(totalExpenses);

        return DashboardSummaryDTO.builder()
                .totalCollection(totalCollection)
                .totalExpenses(totalExpenses)
                .balanceRemaining(balance)
                .carryForwardBalance(carryForward)
                .grandTotalAvailableFunds(grandTotal)
                .activeFestivalYearLabel(activeYear != null ? activeYear.getLabel() : null)
                .expenseByCategory(expenseByCategory)
                .monthlyTrend(monthlyTrend)
                .recentTransactions(recentDTOs)
                .build();
    }
}
