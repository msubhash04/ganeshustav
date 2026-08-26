package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.LoanDtos.*;
import com.ganeshutsav.backend.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Post-festival micro-lending is restricted to the President, per the
 * "President: ... manage post-festival loans" role definition.
 */
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRESIDENT')")
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    public List<LoanResponse> getAll() {
        return loanService.getAll();
    }

    @GetMapping("/{id}")
    public LoanResponse getById(@PathVariable Long id) {
        return loanService.getById(id);
    }

    @GetMapping("/outstanding-total")
    public BigDecimal getOutstandingTotal() {
        return loanService.getTotalOutstandingPrincipal();
    }

    @PostMapping
    public LoanResponse create(@Valid @RequestBody LoanRequest req) {
        return loanService.createLoan(req);
    }

    @PostMapping("/{id}/repayments")
    public LoanResponse recordRepayment(@PathVariable Long id, @Valid @RequestBody RepaymentRequest req) {
        return loanService.recordRepayment(id, req);
    }
}
