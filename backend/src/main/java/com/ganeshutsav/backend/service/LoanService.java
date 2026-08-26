package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.LoanDtos.*;
import com.ganeshutsav.backend.entity.*;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import com.ganeshutsav.backend.repository.LoanRepaymentRepository;
import com.ganeshutsav.backend.repository.LoanRepository;
import com.ganeshutsav.backend.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Post-festival micro-lending with REDUCING BALANCE monthly interest.
 * (Calculation logic unchanged from before multi-tenancy - see the
 * detailed walkthrough in git history / earlier docs. This pass only
 * adds committee isolation: every loan belongs to exactly one committee,
 * and every method here verifies that before reading or writing.)
 *
 * Example: ₹10,000 at 2%/month. After 6 months, borrower pays ₹6,200.
 *   interest = 10000 * 0.02 * 6 = 1,200
 *   remainder = 6,200 - 1,200 = 5,000 -> applied to principal
 *   new principal = 10,000 - 5,000 = 5,000
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final FestivalYearRepository festivalYearRepository;
    private final TenantContext tenantContext;

    private static final int SCALE = 2;

    @Transactional
    public LoanResponse createLoan(LoanRequest req) {
        Committee committee = tenantContext.requireCommittee();

        FestivalYear year = null;
        if (req.getFestivalYearId() != null) {
            year = festivalYearRepository.findById(req.getFestivalYearId())
                    .filter(y -> y.getCommittee().getId().equals(committee.getId()))
                    .orElseThrow(() -> new EntityNotFoundException("Festival year not found: " + req.getFestivalYearId()));
        }

        Loan loan = Loan.builder()
                .committee(committee)
                .festivalYear(year)
                .borrowerName(req.getBorrowerName())
                .borrowerPhone(req.getBorrowerPhone())
                .originalPrincipal(req.getPrincipalAmount())
                .currentPrincipal(req.getPrincipalAmount())
                .monthlyInterestRatePercent(req.getMonthlyInterestRatePercent())
                .loanDate(req.getLoanDate())
                .lastInterestDate(req.getLoanDate())
                .status(LoanStatus.ACTIVE)
                .recordedBy(tenantContext.getCurrentMember())
                .build();

        loan = loanRepository.save(loan);
        return toResponse(loan);
    }

    public List<LoanResponse> getAll() {
        Long committeeId = tenantContext.requireCommitteeId();
        return loanRepository.findByCommitteeIdOrderByLoanDateDesc(committeeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public LoanResponse getById(Long id) {
        return toResponse(findOwnedLoan(id));
    }

    public BigDecimal getTotalOutstandingPrincipal() {
        return loanRepository.getTotalOutstandingPrincipal(tenantContext.requireCommitteeId());
    }

    @Transactional
    public LoanResponse recordRepayment(Long loanId, RepaymentRequest req) {
        Loan loan = findOwnedLoan(loanId);

        if (req.getPaymentDate().isBefore(loan.getLastInterestDate())) {
            throw new IllegalArgumentException(
                    "Payment date cannot be before the loan's last interest calculation date (" + loan.getLastInterestDate() + ")");
        }

        long monthsElapsed = Period.between(loan.getLastInterestDate(), req.getPaymentDate()).toTotalMonths();

        BigDecimal rate = loan.getMonthlyInterestRatePercent().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal interestDue = loan.getCurrentPrincipal()
                .multiply(rate)
                .multiply(BigDecimal.valueOf(monthsElapsed))
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal payment = req.getPaymentAmount();

        BigDecimal interestPortion;
        BigDecimal principalPortion;

        if (payment.compareTo(interestDue) >= 0) {
            interestPortion = interestDue;
            principalPortion = payment.subtract(interestDue);
        } else {
            interestPortion = payment;
            principalPortion = BigDecimal.ZERO;
        }

        BigDecimal newPrincipal = loan.getCurrentPrincipal().subtract(principalPortion);
        if (newPrincipal.compareTo(BigDecimal.ZERO) < 0) {
            principalPortion = loan.getCurrentPrincipal();
            newPrincipal = BigDecimal.ZERO;
        }

        LoanRepayment repayment = LoanRepayment.builder()
                .loan(loan)
                .paymentDate(req.getPaymentDate())
                .paymentAmount(payment)
                .interestPortion(interestPortion)
                .principalPortion(principalPortion)
                .remainingPrincipalAfter(newPrincipal)
                .recordedBy(tenantContext.getCurrentMember())
                .build();
        loanRepaymentRepository.save(repayment);

        loan.setCurrentPrincipal(newPrincipal);
        loan.setLastInterestDate(req.getPaymentDate());
        if (newPrincipal.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.CLOSED);
        }
        loanRepository.save(loan);

        return toResponse(loan);
    }

    private BigDecimal calculateAccruedInterestAsOfToday(Loan loan) {
        if (loan.getStatus() == LoanStatus.CLOSED) return BigDecimal.ZERO;
        long monthsElapsed = Period.between(loan.getLastInterestDate(), LocalDate.now()).toTotalMonths();
        if (monthsElapsed <= 0) return BigDecimal.ZERO;
        BigDecimal rate = loan.getMonthlyInterestRatePercent().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return loan.getCurrentPrincipal()
                .multiply(rate)
                .multiply(BigDecimal.valueOf(monthsElapsed))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    // loads by id, then verifies it belongs to the caller's own committee
    private Loan findOwnedLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found: " + id));
        tenantContext.assertOwnedByCurrentTenant(loan.getCommittee());
        return loan;
    }

    private LoanResponse toResponse(Loan loan) {
        List<RepaymentResponse> repayments = loanRepaymentRepository.findByLoanIdOrderByPaymentDateAsc(loan.getId())
                .stream()
                .map(r -> RepaymentResponse.builder()
                        .id(r.getId())
                        .paymentDate(r.getPaymentDate())
                        .paymentAmount(r.getPaymentAmount())
                        .interestPortion(r.getInterestPortion())
                        .principalPortion(r.getPrincipalPortion())
                        .remainingPrincipalAfter(r.getRemainingPrincipalAfter())
                        .build())
                .collect(Collectors.toList());

        return LoanResponse.builder()
                .id(loan.getId())
                .borrowerName(loan.getBorrowerName())
                .borrowerPhone(loan.getBorrowerPhone())
                .originalPrincipal(loan.getOriginalPrincipal())
                .currentPrincipal(loan.getCurrentPrincipal())
                .monthlyInterestRatePercent(loan.getMonthlyInterestRatePercent())
                .loanDate(loan.getLoanDate())
                .lastInterestDate(loan.getLastInterestDate())
                .status(loan.getStatus())
                .accruedInterestAsOfToday(calculateAccruedInterestAsOfToday(loan))
                .repayments(repayments)
                .build();
    }
}
