package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import com.ganeshutsav.backend.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared "Active Festival Guard" used by every festival-year-scoped
 * domain service (Donations, Expenses, Auction, General/Annadanam
 * Sponsors) so that:
 *   1. A committee can never record a new collection/expense/sponsor/
 *      auction item without an active festival year.
 *   2. A record can never be filed against, or later modified under,
 *      a festival year that has since been archived (superseded by a
 *      newer year - see FestivalYearService.create(), which is what
 *      actually flips the previous year's "active" flag off).
 * Read-only access to an archived year (for the Festival Archives /
 * Audit Report feature) deliberately does NOT go through this guard -
 * see loadOwned() below, which has no active check at all.
 */
@Component
@RequiredArgsConstructor
public class FestivalYearGuard {

    public static final String NO_ACTIVE_YEAR_MESSAGE =
            "First create the Festival year to unlock these features.";

    private final FestivalYearRepository festivalYearRepository;
    private final TenantContext tenantContext;

    /**
     * The caller's committee's currently active festival year, or throws
     * if none exists.
     *
     * RULE: an active year must also actually BE the current calendar
     * year - not just have its "active" flag left on from before. If a
     * committee never creates a new year's festival, last year's record
     * would otherwise stay flagged active forever once the calendar
     * rolls over on Jan 1, silently letting new records get filed under
     * a festival that's already over. Nothing automatically flips that
     * flag off at midnight, so this check (not just the flag) is what
     * actually re-locks these modules until the new year's setup exists.
     */
    public FestivalYear requireActiveYear() {
        FestivalYear year = findActiveYearForCurrentCalendarYear();
        if (year == null) {
            throw new IllegalStateException(NO_ACTIVE_YEAR_MESSAGE);
        }
        return year;
    }

    /** Same as requireActiveYear(), but returns null instead of throwing - for read-only status checks. */
    public FestivalYear findActiveYearForCurrentCalendarYear() {
        Long committeeId = tenantContext.requireCommitteeId();
        int currentYear = java.time.LocalDate.now().getYear();
        return festivalYearRepository.findFirstByCommitteeIdAndActiveTrueOrderByIdDesc(committeeId)
                .filter(y -> y.getYear() == currentYear)
                .orElse(null);
    }

    /**
     * Resolves the festival year a NEW record should be filed under: the
     * explicitly requested one (must belong to this committee AND
     * currently be active), or - when none is specified - whichever
     * year is currently active. Either way, the year returned is
     * guaranteed active, so a new record can never be silently filed
     * against an archived year.
     */
    public FestivalYear resolveForNewRecord(Long requestedFestivalYearId) {
        if (requestedFestivalYearId == null) {
            return requireActiveYear();
        }
        FestivalYear year = loadOwned(requestedFestivalYearId);
        assertActive(year);
        return year;
    }

    /** Loads a festival year the caller's committee owns, for READ-ONLY purposes - no active check. */
    public FestivalYear loadOwned(Long festivalYearId) {
        FestivalYear year = festivalYearRepository.findById(festivalYearId)
                .orElseThrow(() -> new EntityNotFoundException("Festival year not found: " + festivalYearId));
        tenantContext.assertOwnedByCurrentTenant(year.getCommittee());
        return year;
    }

    /** Same as loadOwned(), but for a WRITE - additionally requires the year still be active. */
    public FestivalYear loadOwnedActive(Long festivalYearId) {
        FestivalYear year = loadOwned(festivalYearId);
        assertActive(year);
        return year;
    }

    /** Call before modifying or deleting an EXISTING record - blocks the edit once its year is archived. */
    public void assertActive(FestivalYear year) {
        if (year == null || !year.isActive()) {
            throw new IllegalStateException(
                    "This record belongs to an archived festival year and can no longer be modified.");
        }
    }
}
