package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.LoanDtos.*;
import com.ganeshutsav.backend.entity.*;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import com.ganeshutsav.backend.repository.LoanRepaymentRepository;
import com.ganeshutsav.backend.repository.LoanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
 *
 * On every repayment:
 *   1. Work out how many whole months have passed since the loan's
 *      lastInterestDate (loan start, or the previous repayment date).
 *   2. Interest due = currentPrincipal * (rate/100) * monthsElapsed.
 *   3. If the payment covers the interest due, the excess reduces the
 *      principal directly. If it doesn't fully cover the interest,
 *      the whole payment is treated as interest and the principal is
 *      untouched (the shortfall simply isn't tracked as a separate
 *      arrears figure in this simple model).
 *   4. lastInterestDate moves forward to the payment date, so future
 *      interest is calculated only on the new, lower principal.
 *
 * Example: ₹10,000 at 2%/month. After 6 months, borrower pays ₹6,200.
 *   interest = 10000 * 0.02 * 6 = 1,200
 *   remainder = 6,200 - 1,200 = 5,000 -> applied to principal
 *   new principal = 10,000 - 5,000 = 5,000
 *   Future interest accrues only on ₹5,000 going forward.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final FestivalYearRepository festivalYearRepository;

    private static final int SCALE = 2;

    @Transactional
    public LoanResponse createLoan(LoanRequest req) {
        FestivalYear year = null;
        if (req.getFestivalYearId() != null) {
            year = festivalYearRepository.findById(req.getFestivalYearId())
                    .orElseThrow(() -> new EntityNotFoundException("Festival year not found: " + req.getFestivalYearId()));
        }

        Loan loan = Loan.builder()
                .festivalYear(year)
                .borrowerName(req.getBorrowerName())
                .borrowerPhone(req.getBorrowerPhone())
                .originalPrincipal(req.getPrincipalAmount())
                .currentPrincipal(req.getPrincipalAmount())
                .monthlyInterestRatePercent(req.getMonthlyInterestRatePercent())
                .loanDate(req.getLoanDate())
                .lastInterestDate(req.getLoanDate())
                .status(LoanStatus.ACTIVE)
                .recordedBy(getCurrentMember())
                .build();

        loan = loanRepository.save(loan);
        return toResponse(loan);
    }

    public List<LoanResponse> getAll() {
        return loanRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public LoanResponse getById(Long id) {
        return toResponse(findLoan(id));
    }

    public BigDecimal getTotalOutstandingPrincipal() {
        return loanRepository.getTotalOutstandingPrincipal();
    }

    /**
     * Records a repayment and applies the reducing-balance calculation
     * described above. Returns the updated loan (with its new repayment
     * appended) so the caller can show the borrower exactly how the
     * payment was split.
     */
    @Transactional
    public LoanResponse recordRepayment(Long loanId, RepaymentRequest req) {
        Loan loan = findLoan(loanId);

        if (req.getPaymentDate().isBefore(loan.getLastInterestDate())) {
            throw new IllegalArgumentException(
                    "Payment date cannot be before the loan's last interest calculation date (" + loan.getLastInterestDate() + ")");
        }

        long monthsElapsed = Period.between(loan.getLastInterestDate(), req.getPaymentDate()).toTotalMonths();
        // guard against a same-day / <1 month repayment producing zero interest incorrectly
        // (this is expected behaviour: no interest has accrued yet if less than a month has passed)

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
            // payment doesn't even cover the accrued interest -
            // the whole payment is absorbed as interest, principal untouched
            interestPortion = payment;
            principalPortion = BigDecimal.ZERO;
        }

        BigDecimal newPrincipal = loan.getCurrentPrincipal().subtract(principalPortion);
        if (newPrincipal.compareTo(BigDecimal.ZERO) < 0) {
            // don't let an overpayment push principal negative;
            // cap the principal portion actually applied
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
                .recordedBy(getCurrentMember())
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

    /** Interest accrued from lastInterestDate up to today, at the current principal - informational only. */
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

    private Loan findLoan(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found: " + id));
    }

    private Member getCurrentMember() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
        return (principal instanceof Member) ? (Member) principal : null;
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
