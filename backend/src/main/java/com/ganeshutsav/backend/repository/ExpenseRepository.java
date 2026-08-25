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

    List<Expense> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal getTotalExpenses();

    @Query("SELECT e.category as category, COALESCE(SUM(e.amount),0) as total FROM Expense e GROUP BY e.category")
    List<CategoryTotal> getCategoryWiseTotals();

    @Query("SELECT e FROM Expense e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:startDate IS NULL OR e.expenseDate >= :startDate) AND " +
           "(:endDate IS NULL OR e.expenseDate <= :endDate) " +
           "ORDER BY e.expenseDate DESC")
    List<Expense> search(@Param("category") ExpenseCategory category,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);

    List<Expense> findByExpenseDateBetween(LocalDate start, LocalDate end);

    List<Expense> findByFestivalYearIdOrderByDayNumberAsc(Long festivalYearId);

    @Query("SELECT e.dayNumber as dayNumber, COALESCE(SUM(e.amount),0) as total FROM Expense e " +
           "WHERE e.festivalYear.id = :festivalYearId GROUP BY e.dayNumber ORDER BY e.dayNumber")
    List<DayTotal> getDayWiseTotals(@Param("festivalYearId") Long festivalYearId);

    interface DayTotal {
        Integer getDayNumber();
        BigDecimal getTotal();
    }

    interface CategoryTotal {
        ExpenseCategory getCategory();
        BigDecimal getTotal();
    }
}
