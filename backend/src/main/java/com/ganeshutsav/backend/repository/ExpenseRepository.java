package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.Expense;
import com.ganeshutsav.backend.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCommitteeIdOrderByExpenseDateDesc(Long committeeId);
    List<Expense> findTop10ByCommitteeIdOrderByCreatedAtDesc(Long committeeId);
    List<Expense> findByFestivalYearIdOrderByExpenseDateDesc(Long festivalYearId);
    List<Expense> findTop10ByFestivalYearIdOrderByCreatedAtDesc(Long festivalYearId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.committee.id = :committeeId")
    BigDecimal getTotalExpenses(@Param("committeeId") Long committeeId);

    // scoped to a single festival year - used by the Dashboard (active
    // year only) and the Festival Archives audit report (any owned year)
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.festivalYear.id = :festivalYearId")
    BigDecimal getTotalExpensesByFestivalYear(@Param("festivalYearId") Long festivalYearId);

    @Query("SELECT e.category as category, COALESCE(SUM(e.amount),0) as total FROM Expense e " +
           "WHERE e.committee.id = :committeeId GROUP BY e.category")
    List<CategoryTotal> getCategoryWiseTotals(@Param("committeeId") Long committeeId);

    @Query("SELECT e.category as category, COALESCE(SUM(e.amount),0) as total FROM Expense e " +
           "WHERE e.festivalYear.id = :festivalYearId GROUP BY e.category")
    List<CategoryTotal> getCategoryWiseTotalsByFestivalYear(@Param("festivalYearId") Long festivalYearId);

    // MULTI-TENANT SAFETY: committeeId always comes from the authenticated
    // caller's own committee via TenantContext - never from client input
    @Query("SELECT e FROM Expense e WHERE e.committee.id = :committeeId AND " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:startDate IS NULL OR e.expenseDate >= :startDate) AND " +
           "(:endDate IS NULL OR e.expenseDate <= :endDate) " +
           "ORDER BY e.expenseDate DESC")
    List<Expense> search(@Param("committeeId") Long committeeId,
                          @Param("category") ExpenseCategory category,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);

    List<Expense> findByCommitteeIdAndExpenseDateBetween(Long committeeId, LocalDate start, LocalDate end);

    List<Expense> findByFestivalYearIdOrderByDayNumberAsc(Long festivalYearId);

    @Query("SELECT e.dayNumber as dayNumber, COALESCE(SUM(e.amount),0) as total FROM Expense e " +
           "WHERE e.festivalYear.id = :festivalYearId GROUP BY e.dayNumber ORDER BY e.dayNumber")
    List<DayTotal> getDayWiseTotals(@Param("festivalYearId") Long festivalYearId);

    // used by the Developer's global overview dashboard - intentionally NOT
    // committee-scoped, and only ever called from DEVELOPER-gated code paths
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal getTotalExpensesAllCommittees();

    interface DayTotal {
        Integer getDayNumber();
        BigDecimal getTotal();
    }

    interface CategoryTotal {
        ExpenseCategory getCategory();
        BigDecimal getTotal();
    }
}
