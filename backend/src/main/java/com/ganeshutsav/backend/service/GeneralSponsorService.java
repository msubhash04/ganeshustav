package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.SponsorshipDtos.GeneralSponsorDTO;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.entity.GeneralSponsor;
import com.ganeshutsav.backend.entity.SponsorshipCategory;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import com.ganeshutsav.backend.repository.GeneralSponsorRepository;
import com.ganeshutsav.backend.repository.SponsorshipCategoryRepository;
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
public class GeneralSponsorService {

    private final GeneralSponsorRepository generalSponsorRepository;
    private final SponsorshipCategoryRepository categoryRepository;
    private final FestivalYearRepository festivalYearRepository;
    private final TenantContext tenantContext;

    public List<GeneralSponsorDTO> getAll() {
        Long committeeId = tenantContext.requireCommitteeId();
        return generalSponsorRepository.findByCommitteeIdOrderByCreatedAtDesc(committeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<GeneralSponsorDTO> getByFestivalYear(Long festivalYearId) {
        FestivalYear year = findOwnedFestivalYear(festivalYearId);
        return generalSponsorRepository.findByFestivalYearIdOrderByCreatedAtDesc(year.getId())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public GeneralSponsorDTO create(GeneralSponsorDTO dto) {
        Committee committee = tenantContext.requireCommittee();
        SponsorshipCategory category = findOwnedCategory(dto.getCategoryId());
        FestivalYear year = resolveFestivalYear(dto.getFestivalYearId());

        GeneralSponsor sponsor = GeneralSponsor.builder()
                .committee(committee)
                .sponsorName(dto.getSponsorName())
                .contactInfo(dto.getContactInfo())
                .contributionAmount(dto.getContributionAmount())
                .contributionDetails(dto.getContributionDetails())
                .category(category)
                .festivalYear(year)
                .recordedBy(tenantContext.getCurrentMember())
                .build();
        return toDTO(generalSponsorRepository.save(sponsor));
    }

    @Transactional
    public GeneralSponsorDTO update(Long id, GeneralSponsorDTO dto) {
        GeneralSponsor existing = findOwnedEntity(id);
        SponsorshipCategory category = findOwnedCategory(dto.getCategoryId());

        existing.setSponsorName(dto.getSponsorName());
        existing.setContactInfo(dto.getContactInfo());
        existing.setContributionAmount(dto.getContributionAmount());
        existing.setContributionDetails(dto.getContributionDetails());
        existing.setCategory(category);
        return toDTO(generalSponsorRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        findOwnedEntity(id); // verifies ownership before deleting
        generalSponsorRepository.deleteById(id);
    }

    // guards against a categoryId belonging to a different committee
    private SponsorshipCategory findOwnedCategory(Long categoryId) {
        SponsorshipCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Sponsorship category not found: " + categoryId));
        tenantContext.assertOwnedByCurrentTenant(category.getCommittee());
        return category;
    }

    // guards against a festivalYearId belonging to a different committee
    private FestivalYear findOwnedFestivalYear(Long festivalYearId) {
        FestivalYear year = festivalYearRepository.findById(festivalYearId)
                .orElseThrow(() -> new EntityNotFoundException("Festival year not found: " + festivalYearId));
        tenantContext.assertOwnedByCurrentTenant(year.getCommittee());
        return year;
    }

    private FestivalYear resolveFestivalYear(Long festivalYearId) {
        if (festivalYearId != null) {
            return findOwnedFestivalYear(festivalYearId);
        }
        return festivalYearRepository.findFirstByCommitteeIdAndActiveTrueOrderByIdDesc(tenantContext.requireCommitteeId()).orElse(null);
    }

    private GeneralSponsor findOwnedEntity(Long id) {
        GeneralSponsor sponsor = generalSponsorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("General sponsor not found: " + id));
        tenantContext.assertOwnedByCurrentTenant(sponsor.getCommittee());
        return sponsor;
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
