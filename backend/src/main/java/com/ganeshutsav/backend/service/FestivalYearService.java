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

    public List<FestivalYearDTO> getAll() {
        Long committeeId = tenantContext.requireCommitteeId();
        return festivalYearRepository.findByCommitteeIdOrderByYearDesc(committeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public FestivalYearDTO getById(Long id) {
        return toDTO(findOwnedEntity(id));
    }

    public FestivalYearDTO getActive() {
        Long committeeId = tenantContext.requireCommitteeId();
        return festivalYearRepository.findFirstByCommitteeIdAndActiveTrueOrderByIdDesc(committeeId)
                .map(this::toDTO).orElse(null);
    }

    @Transactional
    public FestivalYearDTO create(FestivalYearDTO dto) {
        Committee committee = tenantContext.requireCommittee();

        // clears ALL active flags WITHIN THIS COMMITTEE ONLY - never touches
        // any other committee's data - so there's only ever one default per committee
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
        existing.setLabel(dto.getLabel());
        existing.setStartDate(dto.getStartDate());
        existing.setDurationDays(dto.getDurationDays());
        existing.setCarryForwardBalance(dto.getCarryForwardBalance());
        return toDTO(festivalYearRepository.save(existing));
    }

    @Transactional
    public FestivalYearDTO setActive(Long id) {
        FestivalYear year = findOwnedEntity(id);
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
