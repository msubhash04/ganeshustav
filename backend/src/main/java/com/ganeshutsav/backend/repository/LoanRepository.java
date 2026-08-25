package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.Loan;
import com.ganeshutsav.backend.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByStatus(LoanStatus status);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(l.currentPrincipal),0) FROM Loan l WHERE l.status = 'ACTIVE'")
    BigDecimal getTotalOutstandingPrincipal();
}
