package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {
    List<LoanRepayment> findByLoanIdOrderByPaymentDateAsc(Long loanId);
}
