package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.Loan;
import com.ganeshutsav.backend.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByCommitteeIdOrderByLoanDateDesc(Long committeeId);
    List<Loan> findByCommitteeIdAndStatus(Long committeeId, LoanStatus status);

    @Query("SELECT COALESCE(SUM(l.currentPrincipal),0) FROM Loan l WHERE l.committee.id = :committeeId AND l.status = 'ACTIVE'")
    BigDecimal getTotalOutstandingPrincipal(@Param("committeeId") Long committeeId);

    // used by the Developer's global overview dashboard - intentionally NOT
    // committee-scoped, and only ever called from DEVELOPER-gated code paths
    @Query("SELECT COALESCE(SUM(l.currentPrincipal),0) FROM Loan l WHERE l.status = 'ACTIVE'")
    BigDecimal getTotalOutstandingPrincipalAllCommittees();
}
