package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.FestivalYearDTO;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FestivalYearService {

    private final FestivalYearRepository festivalYearRepository;

    public List<FestivalYearDTO> getAll() {
        return festivalYearRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public FestivalYearDTO getById(Long id) {
        return toDTO(findEntity(id));
    }

    public FestivalYearDTO getActive() {
        return festivalYearRepository.findFirstByActiveTrueOrderByIdDesc().map(this::toDTO).orElse(null);
    }

    @Transactional
    public FestivalYearDTO create(FestivalYearDTO dto) {
        // clears ALL active flags first (not just one) so this self-heals even if
        // multiple rows were somehow left marked active - there's only ever one default
        festivalYearRepository.deactivateAll();

        FestivalYear year = FestivalYear.builder()
                .label(dto.getLabel())
                .year(dto.getYear())
                .startDate(dto.getStartDate())
                .durationDays(dto.getDurationDays())
                .carryForwardBalance(dto.getCarryForwardBalance())
                .active(true)
                .createdBy(getCurrentMember())
                .build();
        return toDTO(festivalYearRepository.save(year));
    }

    // President can edit the date / duration (and other fields) after creation
    @Transactional
    public FestivalYearDTO update(Long id, FestivalYearDTO dto) {
        FestivalYear existing = findEntity(id);
        existing.setLabel(dto.getLabel());
        existing.setStartDate(dto.getStartDate());
        existing.setDurationDays(dto.getDurationDays());
        existing.setCarryForwardBalance(dto.getCarryForwardBalance());
        return toDTO(festivalYearRepository.save(existing));
    }

    @Transactional
    public FestivalYearDTO setActive(Long id) {
        festivalYearRepository.deactivateAll();
        FestivalYear year = findEntity(id);
        year.setActive(true);
        return toDTO(festivalYearRepository.save(year));
    }

    private FestivalYear findEntity(Long id) {
        return festivalYearRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Festival year not found: " + id));
    }

    private Member getCurrentMember() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
        return (principal instanceof Member) ? (Member) principal : null;
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
