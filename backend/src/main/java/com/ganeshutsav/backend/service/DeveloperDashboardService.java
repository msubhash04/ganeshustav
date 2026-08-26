package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.DeveloperDashboardDTO;
import com.ganeshutsav.backend.repository.CommitteeRepository;
import com.ganeshutsav.backend.repository.DonationRepository;
import com.ganeshutsav.backend.repository.ExpenseRepository;
import com.ganeshutsav.backend.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

/**
 * Powers the Developer (Super Admin) Dashboard's global overview widgets.
 * Deliberately kept separate from DashboardService (the committee-scoped
 * one) so there's no risk of a President/Member ever calling into
 * cross-tenant aggregation by accident - access is gated at the
 * controller layer via @PreAuthorize("hasRole('DEVELOPER')").
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeveloperDashboardService {

    private final CommitteeRepository committeeRepository;
    private final DonationRepository donationRepository;
    private final ExpenseRepository expenseRepository;
    private final LoanRepository loanRepository;
    private final com.ganeshutsav.backend.repository.FestivalYearRepository festivalYearRepository;

    public DeveloperDashboardDTO getGlobalOverview() {
        return DeveloperDashboardDTO.builder()
                .totalRegisteredCommittees(committeeRepository.count())
                .activeCommittees(committeeRepository.countByActiveTrue())
                .activeUtsavsThisYear(festivalYearRepository.countActiveUtsavsForYear(Year.now().getValue()))
                .totalCollectionsAllCommittees(donationRepository.getTotalCollectionAllCommittees())
                .totalExpensesAllCommittees(expenseRepository.getTotalExpensesAllCommittees())
                .totalLentMoneyAllCommittees(loanRepository.getTotalOutstandingPrincipalAllCommittees())
                .build();
    }
}
