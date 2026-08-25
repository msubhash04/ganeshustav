package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.SponsorshipDtos.GeneralSponsorDTO;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.entity.GeneralSponsor;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.entity.SponsorshipCategory;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import com.ganeshutsav.backend.repository.GeneralSponsorRepository;
import com.ganeshutsav.backend.repository.SponsorshipCategoryRepository;
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
public class GeneralSponsorService {

    private final GeneralSponsorRepository generalSponsorRepository;
    private final SponsorshipCategoryRepository categoryRepository;
    private final FestivalYearRepository festivalYearRepository;

    public List<GeneralSponsorDTO> getAll() {
        return generalSponsorRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<GeneralSponsorDTO> getByFestivalYear(Long festivalYearId) {
        return generalSponsorRepository.findByFestivalYearIdOrderByCreatedAtDesc(festivalYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public GeneralSponsorDTO create(GeneralSponsorDTO dto) {
        SponsorshipCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Sponsorship category not found: " + dto.getCategoryId()));
        FestivalYear year = resolveFestivalYear(dto.getFestivalYearId());

        GeneralSponsor sponsor = GeneralSponsor.builder()
                .sponsorName(dto.getSponsorName())
                .contactInfo(dto.getContactInfo())
                .contributionAmount(dto.getContributionAmount())
                .contributionDetails(dto.getContributionDetails())
                .category(category)
                .festivalYear(year)
                .recordedBy(getCurrentMember())
                .build();
        return toDTO(generalSponsorRepository.save(sponsor));
    }

    @Transactional
    public GeneralSponsorDTO update(Long id, GeneralSponsorDTO dto) {
        GeneralSponsor existing = findEntity(id);
        SponsorshipCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Sponsorship category not found: " + dto.getCategoryId()));

        existing.setSponsorName(dto.getSponsorName());
        existing.setContactInfo(dto.getContactInfo());
        existing.setContributionAmount(dto.getContributionAmount());
        existing.setContributionDetails(dto.getContributionDetails());
        existing.setCategory(category);
        return toDTO(generalSponsorRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!generalSponsorRepository.existsById(id)) {
            throw new EntityNotFoundException("General sponsor not found: " + id);
        }
        generalSponsorRepository.deleteById(id);
    }

    private FestivalYear resolveFestivalYear(Long festivalYearId) {
        if (festivalYearId != null) {
            return festivalYearRepository.findById(festivalYearId).orElse(null);
        }
        return festivalYearRepository.findFirstByActiveTrueOrderByIdDesc().orElse(null);
    }

    private GeneralSponsor findEntity(Long id) {
        return generalSponsorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("General sponsor not found: " + id));
    }

    private Member getCurrentMember() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
        return (principal instanceof Member) ? (Member) principal : null;
    }

    private GeneralSponsorDTO toDTO(GeneralSponsor s) {
        GeneralSponsorDTO dto = new GeneralSponsorDTO();
        dto.setId(s.getId());
        dto.setSponsorName(s.getSponsorName());
        dto.setContactInfo(s.getContactInfo());
        dto.setContributionAmount(s.getContributionAmount());
        dto.setContributionDetails(s.getContributionDetails());
        dto.setCategoryId(s.getCategory() != null ? s.getCategory().getId() : null);
        dto.setCategoryName(s.getCategory() != null ? s.getCategory().getName() : null);
        dto.setFestivalYearId(s.getFestivalYear() != null ? s.getFestivalYear().getId() : null);
        dto.setRecordedByName(s.getRecordedBy() != null ? s.getRecordedBy().getName() : null);
        return dto;
    }
}
