package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.FestivalYearDTO;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import com.ganeshutsav.backend.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FestivalYearService {

    private final FestivalYearRepository festivalYearRepository;
    private final TenantContext tenantContext;
    private final FestivalYearGuard festivalYearGuard;

    public List<FestivalYearDTO> getAll() {
        Long committeeId = tenantContext.requireCommitteeId();
        return festivalYearRepository.findByCommitteeIdOrderByYearDesc(committeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public FestivalYearDTO getById(Long id) {
        return toDTO(findOwnedEntity(id));
    }

    // Used by the frontend's Active Festival Guard (module lock) and by
    // forms that default new records to "this year" - so this must apply
    // the same "active AND actually the current calendar year" check as
    // every write path (see FestivalYearGuard), not just the raw flag.
    public FestivalYearDTO getActive() {
        FestivalYear year = festivalYearGuard.findActiveYearForCurrentCalendarYear();
        return year == null ? null : toDTO(year);
    }

    @Transactional
    public FestivalYearDTO create(FestivalYearDTO dto) {
        Committee committee = tenantContext.requireCommittee();

        // RULE: a festival can only ever be created for the CURRENT
        // calendar year - this single check covers both halves of the
        // spec at once: a past year (2025 in 2026) is rejected because
        // it no longer equals "now", and a future year (2027 in 2026)
        // is rejected too, until the calendar itself rolls over and
        // 2027 *becomes* "now".
        int currentYear = java.time.LocalDate.now().getYear();
        if (dto.getYear() == null || dto.getYear() != currentYear) {
            throw new IllegalStateException(
                    "A festival can only be created for the current calendar year (" + currentYear + "). " +
                    (dto.getYear() != null && dto.getYear() < currentYear
                            ? "Creating festivals for past years is not allowed."
                            : "Future festivals unlock automatically once that year begins."));
        }

        // RULE: only one festival per calendar year, per committee
        if (festivalYearRepository.existsByCommitteeIdAndYear(committee.getId(), dto.getYear())) {
            throw new IllegalStateException("A festival for " + dto.getYear() + " has already been created.");
        }

        // clears ALL active flags WITHIN THIS COMMITTEE ONLY - never touches
        // any other committee's data - so there's only ever one default per committee.
        // This is also what performs the "automatic archiving" of the previous
        // year: the moment a new year's festival is created, every earlier
        // year for this committee flips to inactive/archived in this one call.
        festivalYearRepository.deactivateAllForCommittee(committee.getId());

        FestivalYear year = FestivalYear.builder()
                .committee(committee)
                .label(dto.getLabel())
                .year(dto.getYear())
                .startDate(dto.getStartDate())
                .durationDays(dto.getDurationDays())
                .carryForwardBalance(dto.getCarryForwardBalance())
                .active(true)
                .createdBy(tenantContext.getCurrentMember())
                .build();
        return toDTO(festivalYearRepository.save(year));
    }

    // President can edit the date / duration (and other fields) after creation
    @Transactional
    public FestivalYearDTO update(Long id, FestivalYearDTO dto) {
        FestivalYear existing = findOwnedEntity(id);
        // RULE: an archived festival's own settings can't be edited either,
        // same as the records filed under it - once superseded by a newer
        // year, a festival's setup is frozen for historical accuracy.
        if (!existing.isActive()) {
            throw new IllegalStateException(
                    "This festival year is archived and its setup can no longer be edited.");
        }
        existing.setLabel(dto.getLabel());
        existing.setStartDate(dto.getStartDate());
        existing.setDurationDays(dto.getDurationDays());
        existing.setCarryForwardBalance(dto.getCarryForwardBalance());
        return toDTO(festivalYearRepository.save(existing));
    }

    @Transactional
    public FestivalYearDTO setActive(Long id) {
        FestivalYear year = findOwnedEntity(id);
        // RULE: archived years stay archived - only the current calendar
        // year's festival can ever be (re)activated, otherwise this
        // endpoint would be a backdoor around the whole archiving rule
        // (reopening an old year to new data entry).
        int currentYear = java.time.LocalDate.now().getYear();
        if (year.getYear() != currentYear) {
            throw new IllegalStateException(
                    "Only the current year's (" + currentYear + ") festival can be active. " +
                    "Past festivals stay archived.");
        }
        festivalYearRepository.deactivateAllForCommittee(year.getCommittee().getId());
        year.setActive(true);
        return toDTO(festivalYearRepository.save(year));
    }

    // loads by id, then verifies it belongs to the caller's own committee -
    // this is what actually prevents "President A editing Committee B's year"
    private FestivalYear findOwnedEntity(Long id) {
        FestivalYear year = festivalYearRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Festival year not found: " + id));
        tenantContext.assertOwnedByCurrentTenant(year.getCommittee());
        return year;
    }

    private FestivalYearDTO toDTO(FestivalYear y) {
        FestivalYearDTO dto = new FestivalYearDTO();
        dto.setId(y.getId());
        dto.setLabel(y.getLabel());
        dto.setYear(y.getYear());
        dto.setStartDate(y.getStartDate());
        dto.setDurationDays(y.getDurationDays());
        dto.setCarryForwardBalance(y.getCarryForwardBalance());
        dto.setActive(y.isActive());
        return dto;
    }
}
