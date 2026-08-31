package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.DashboardSummaryDTO;
import com.ganeshutsav.backend.entity.Donation;
import com.ganeshutsav.backend.entity.Expense;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.repository.DonationRepository;
import com.ganeshutsav.backend.repository.ExpenseRepository;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
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
    private final FestivalYearGuard festivalYearGuard;
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    public DashboardSummaryDTO getSummary() {
        // Same "active AND actually the current calendar year" check as
        // every write path (see FestivalYearGuard) - otherwise, past
        // Jan 1, the dashboard would keep showing last year's now-stale
        // "active" festival as if it were still current.
        FestivalYear activeYear = festivalYearGuard.findActiveYearForCurrentCalendarYear();

        // RULE (Dynamic Dashboard Context): the dashboard reflects ONLY
        // the currently active festival year's records, not an all-time
        // cross-year total. The moment a new year is created, every
        // query below automatically scopes to that new year's id instead
        // (see FestivalYearService.create(), which flips "active" over) -
        // no separate "reset" step is needed here.
        if (activeYear == null) {
            return DashboardSummaryDTO.builder()
                    .totalCollection(BigDecimal.ZERO)
                    .totalExpenses(BigDecimal.ZERO)
                    .balanceRemaining(BigDecimal.ZERO)
                    .carryForwardBalance(BigDecimal.ZERO)
                    .grandTotalAvailableFunds(BigDecimal.ZERO)
                    .activeFestivalYearLabel(null)
                    .expenseByCategory(new LinkedHashMap<>())
                    .monthlyTrend(new ArrayList<>())
                    .recentTransactions(new ArrayList<>())
                    .build();
        }

        Long yearId = activeYear.getId();
        BigDecimal totalCollection = donationRepository.getTotalCollectionByFestivalYear(yearId);
        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByFestivalYear(yearId);
        BigDecimal balance = totalCollection.subtract(totalExpenses);

        Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();
        expenseRepository.getCategoryWiseTotalsByFestivalYear(yearId)
                .forEach(row -> expenseByCategory.put(row.getCategory().getLabel(), row.getTotal()));

        Map<String, BigDecimal> collectionsByMonth = new TreeMap<>();
        Map<String, BigDecimal> expensesByMonth = new TreeMap<>();

        for (Donation d : donationRepository.findByFestivalYearIdOrderByDonationDateDesc(yearId)) {
            String key = d.getDonationDate().format(MONTH_FMT);
            collectionsByMonth.merge(key, d.getAmount(), BigDecimal::add);
        }
        for (Expense e : expenseRepository.findByFestivalYearIdOrderByExpenseDateDesc(yearId)) {
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

        // recent transactions: merge last donations + expenses (active year
        // only), sort by createdAt desc, take 10. Converted to plain-value
        // DTOs HERE, inside the transaction, so the entities' lazy fields
        // never need to be touched again after this method returns.
        record Recent(java.time.LocalDateTime createdAt, DashboardSummaryDTO.RecentTransactionDTO dto) {}
        List<Recent> recent = new ArrayList<>();
        for (Donation d : donationRepository.findTop10ByFestivalYearIdOrderByCreatedAtDesc(yearId)) {
            recent.add(new Recent(d.getCreatedAt(), DashboardSummaryDTO.RecentTransactionDTO.builder()
                    .type("COLLECTION")
                    .label(d.getDonorName())
                    .date(d.getDonationDate())
                    .amount(d.getAmount())
                    .build()));
        }
        for (Expense e : expenseRepository.findTop10ByFestivalYearIdOrderByCreatedAtDesc(yearId)) {
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

        BigDecimal carryForward = activeYear.getCarryForwardBalance();
        BigDecimal grandTotal = carryForward.add(totalCollection).subtract(totalExpenses);

        return DashboardSummaryDTO.builder()
                .totalCollection(totalCollection)
                .totalExpenses(totalExpenses)
                .balanceRemaining(balance)
                .carryForwardBalance(carryForward)
                .grandTotalAvailableFunds(grandTotal)
                .activeFestivalYearLabel(activeYear.getLabel())
                .expenseByCategory(expenseByCategory)
                .monthlyTrend(monthlyTrend)
                .recentTransactions(recentDTOs)
                .build();
    }
}
