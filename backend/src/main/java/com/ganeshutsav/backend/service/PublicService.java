package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.PublicYearSummaryDTO.Response;
import com.ganeshutsav.backend.dto.PublicYearSummaryDTO.YearOption;
import com.ganeshutsav.backend.dto.PublicYearSummaryDTO.GeneralSponsor;
import com.ganeshutsav.backend.dto.PublicYearSummaryDTO.AnnadanamSponsor;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.ExpenseCategory;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.repository.AnnadanamSponsorRepository;
import com.ganeshutsav.backend.repository.AuctionItemRepository;
import com.ganeshutsav.backend.repository.CommitteeRepository;
import com.ganeshutsav.backend.repository.DonationRepository;
import com.ganeshutsav.backend.repository.ExpenseRepository;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import com.ganeshutsav.backend.repository.GeneralSponsorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Public, unauthenticated, read-only aggregate views for the landing
 * page's "Public Committee Viewer" (search by code) and "Read-Only
 * Observation Dashboard". Deliberately separate from PublicController's
 * pre-existing /transparency endpoint and its all-time totals (left
 * completely untouched) - these methods are scoped to ONE SPECIFIC
 * festival year (the active one, or any archived one a visitor picks)
 * and never return donor names, phone numbers, winner names, or any
 * other itemized ledger row - aggregate figures only, same privacy
 * guarantee the existing transparency page already makes.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicService {

    private final CommitteeRepository committeeRepository;
    private final FestivalYearRepository festivalYearRepository;
    private final DonationRepository donationRepository;
    private final ExpenseRepository expenseRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final GeneralSponsorRepository generalSponsorRepository;
    private final AnnadanamSponsorRepository annadanamSponsorRepository;

    private Committee findCommittee(String tenantCode) {
        return committeeRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new EntityNotFoundException("No committee found with code: " + tenantCode));
    }

    /**
     * The committee's active festival year for the CURRENT calendar
     * year - same "active AND actually this year" rule as
     * FestivalYearGuard uses internally. Returns found=false (not a
     * 404) when the code is valid but nothing is live right now, so the
     * frontend can distinguish "Invalid Code" from "Festival Not Found /
     * no live festival at the moment".
     */
    public Response getActiveYearSummary(String tenantCode) {
        Committee committee = findCommittee(tenantCode);
        int currentYear = LocalDate.now().getYear();
        FestivalYear active = festivalYearRepository.findFirstByCommitteeIdAndActiveTrueOrderByIdDesc(committee.getId())
                .filter(y -> y.getYear() == currentYear)
                .orElse(null);

        if (active == null) {
            return Response.builder()
                    .committeeName(committee.getName())
                    .tenantCode(committee.getTenantCode())
                    .found(false)
                    .build();
        }
        return buildSummary(committee, active);
    }

    /** Every festival year this committee has ever run - label/year/active flag only, for the archive selector. */
    public List<YearOption> getYearOptions(String tenantCode) {
        Committee committee = findCommittee(tenantCode);
        return festivalYearRepository.findByCommitteeIdOrderByYearDesc(committee.getId()).stream()
                .map(y -> YearOption.builder().id(y.getId()).label(y.getLabel()).year(y.getYear()).active(y.isActive()).build())
                .collect(Collectors.toList());
    }

    /** Aggregate summary for one specific year (active or archived) - the code must own that year. */
    public Response getYearSummary(String tenantCode, Long festivalYearId) {
        Committee committee = findCommittee(tenantCode);
        FestivalYear year = festivalYearRepository.findById(festivalYearId)
                .filter(y -> y.getCommittee().getId().equals(committee.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Festival year not found for this committee"));
        return buildSummary(committee, year);
    }

    private Response buildSummary(Committee committee, FestivalYear year) {
        Long yearId = year.getId();

        BigDecimal totalCollections = donationRepository.getTotalCollectionByFestivalYear(yearId);
        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByFestivalYear(yearId);
        BigDecimal totalAuctionEarnings = auctionItemRepository.getTotalAuctionAmount(yearId);
        BigDecimal generalSponsorshipTotal = generalSponsorRepository.getTotalContributionByFestivalYear(yearId);
        BigDecimal annadanamSponsorshipTotal = annadanamSponsorRepository.getTotalContributionByFestivalYear(yearId);
        BigDecimal totalSponsorships = generalSponsorshipTotal.add(annadanamSponsorshipTotal);
        BigDecimal carryForward = year.getCarryForwardBalance() != null ? year.getCarryForwardBalance() : BigDecimal.ZERO;

        // Same formula as the President-facing Festival Audit Report:
        // carryForward + collections + sponsorships + auction earnings - expenses
        BigDecimal netSurplusOrDeficit = carryForward
                .add(totalCollections)
                .add(totalSponsorships)
                .add(totalAuctionEarnings)
                .subtract(totalExpenses);

        Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();
        for (ExpenseRepository.CategoryTotal ct : expenseRepository.getCategoryWiseTotalsByFestivalYear(yearId)) {
            ExpenseCategory category = ct.getCategory();
            expenseByCategory.put(category != null ? category.getLabel() : "Uncategorized", ct.getTotal());
        }

        // Named sponsors, deliberately - see the class-level note on
        // PublicYearSummaryDTO for why this differs from every other
        // field here (donor names, phone numbers, etc. stay private).
        List<GeneralSponsor> generalSponsors = generalSponsorRepository.findByFestivalYearIdOrderByCreatedAtDesc(yearId)
                .stream()
                .map(s -> GeneralSponsor.builder()
                        .sponsorName(s.getSponsorName())
                        .categoryName(s.getCategory() != null ? s.getCategory().getName() : "General")
                        .contributionAmount(s.getContributionAmount())
                        .contributionDetails(s.getContributionDetails())
                        .build())
                .collect(Collectors.toList());

        List<AnnadanamSponsor> annadanamSponsors = annadanamSponsorRepository.findByFestivalYearIdOrderByDayNumberAsc(yearId)
                .stream()
                .map(s -> AnnadanamSponsor.builder()
                        .sponsorName(s.getSponsorName())
                        .dayNumber(s.getDayNumber())
                        .mealSlot(s.getMealSlot())
                        .contributionAmount(s.getContributionAmount())
                        .contributionDetails(s.getContributionDetails())
                        .build())
                .collect(Collectors.toList());

        return Response.builder()
                .committeeName(committee.getName())
                .tenantCode(committee.getTenantCode())
                .found(true)
                .festivalYearId(year.getId())
                .label(year.getLabel())
                .year(year.getYear())
                .startDate(year.getStartDate())
                .durationDays(year.getDurationDays())
                .active(year.isActive())
                .totalCollections(totalCollections)
                .totalExpenses(totalExpenses)
                .totalSponsorships(totalSponsorships)
                .totalAuctionEarnings(totalAuctionEarnings)
                .netSurplusOrDeficit(netSurplusOrDeficit)
                .expenseByCategory(expenseByCategory)
                .totalDonorCount(donationRepository.countByFestivalYearId(yearId))
                .generalSponsors(generalSponsors)
                .annadanamSponsors(annadanamSponsors)
                .build();
    }
}
