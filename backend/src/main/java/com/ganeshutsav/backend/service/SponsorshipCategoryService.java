package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.SponsorshipDtos.CategoryDTO;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.SponsorshipCategory;
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
public class SponsorshipCategoryService {

    private final SponsorshipCategoryRepository categoryRepository;
    private final GeneralSponsorRepository generalSponsorRepository;
    private final TenantContext tenantContext;

    public List<CategoryDTO> getAll() {
        Long committeeId = tenantContext.requireCommitteeId();
        return categoryRepository.findByCommitteeIdOrderByNameAsc(committeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // used to populate the "Sponsorship Category" dropdown on the General Sponsors page
    public List<CategoryDTO> getActive() {
        Long committeeId = tenantContext.requireCommitteeId();
        return categoryRepository.findByCommitteeIdAndActiveTrueOrderByNameAsc(committeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO create(CategoryDTO dto) {
        Committee committee = tenantContext.requireCommittee();
        if (categoryRepository.existsByCommitteeIdAndNameIgnoreCase(committee.getId(), dto.getName())) {
            throw new IllegalArgumentException("A sponsorship category named '" + dto.getName() + "' already exists");
        }
        SponsorshipCategory category = SponsorshipCategory.builder()
                .committee(committee)
                .name(dto.getName())
                .description(dto.getDescription())
                .active(true)
                .createdBy(tenantContext.getCurrentMember())
                .build();
        return toDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        SponsorshipCategory existing = findOwnedEntity(id);
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setActive(dto.isActive());
        return toDTO(categoryRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        SponsorshipCategory existing = findOwnedEntity(id);
        if (generalSponsorRepository.existsByCategoryId(existing.getId())) {
            throw new IllegalStateException(
                    "Cannot delete this category - one or more sponsors are already assigned to it. " +
                    "Mark it inactive instead to hide it from new sponsorships.");
        }
        categoryRepository.deleteById(id);
    }

    // loads by id, then verifies it belongs to the caller's own committee
    private SponsorshipCategory findOwnedEntity(Long id) {
        SponsorshipCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sponsorship category not found: " + id));
        tenantContext.assertOwnedByCurrentTenant(category.getCommittee());
        return category;
    }

    private CategoryDTO toDTO(SponsorshipCategory c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        dto.setActive(c.isActive());
        return dto;
    }
}
