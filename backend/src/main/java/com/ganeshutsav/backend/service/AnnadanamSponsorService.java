package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.SponsorshipDtos.AnnadanamSponsorDTO;
import com.ganeshutsav.backend.entity.AnnadanamSponsor;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.repository.AnnadanamSponsorRepository;
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
public class AnnadanamSponsorService {

    private final AnnadanamSponsorRepository annadanamSponsorRepository;
    private final FestivalYearRepository festivalYearRepository;
    private final TenantContext tenantContext;

    public List<AnnadanamSponsorDTO> getByFestivalYear(Long festivalYearId) {
        FestivalYear year = findOwnedFestivalYear(festivalYearId);
        return annadanamSponsorRepository.findByFestivalYearIdOrderByDayNumberAsc(year.getId())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public AnnadanamSponsorDTO create(AnnadanamSponsorDTO dto) {
        Committee committee = tenantContext.requireCommittee();
        FestivalYear year = resolveFestivalYear(dto.getFestivalYearId());

        AnnadanamSponsor sponsor = AnnadanamSponsor.builder()
                .committee(committee)
                .sponsorName(dto.getSponsorName())
                .contactInfo(dto.getContactInfo())
                .dayNumber(dto.getDayNumber())
                .mealSlot(dto.getMealSlot())
                .contributionAmount(dto.getContributionAmount())
                .contributionDetails(dto.getContributionDetails())
                .festivalYear(year)
                .recordedBy(tenantContext.getCurrentMember())
                .build();
        return toDTO(annadanamSponsorRepository.save(sponsor));
    }

    @Transactional
    public AnnadanamSponsorDTO update(Long id, AnnadanamSponsorDTO dto) {
        AnnadanamSponsor existing = findOwnedEntity(id);
        existing.setSponsorName(dto.getSponsorName());
        existing.setContactInfo(dto.getContactInfo());
        existing.setDayNumber(dto.getDayNumber());
        existing.setMealSlot(dto.getMealSlot());
        existing.setContributionAmount(dto.getContributionAmount());
        existing.setContributionDetails(dto.getContributionDetails());
        return toDTO(annadanamSponsorRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        findOwnedEntity(id); // verifies ownership before deleting
        annadanamSponsorRepository.deleteById(id);
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

    private AnnadanamSponsor findOwnedEntity(Long id) {
        AnnadanamSponsor sponsor = annadanamSponsorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Annadanam sponsor not found: " + id));
        tenantContext.assertOwnedByCurrentTenant(sponsor.getCommittee());
        return sponsor;
    }

    private AnnadanamSponsorDTO toDTO(AnnadanamSponsor s) {
        AnnadanamSponsorDTO dto = new AnnadanamSponsorDTO();
        dto.setId(s.getId());
        dto.setSponsorName(s.getSponsorName());
        dto.setContactInfo(s.getContactInfo());
        dto.setDayNumber(s.getDayNumber());
        dto.setMealSlot(s.getMealSlot());
        dto.setContributionAmount(s.getContributionAmount());
        dto.setContributionDetails(s.getContributionDetails());
        dto.setFestivalYearId(s.getFestivalYear() != null ? s.getFestivalYear().getId() : null);
        dto.setRecordedByName(s.getRecordedBy() != null ? s.getRecordedBy().getName() : null);
        return dto;
    }
}
