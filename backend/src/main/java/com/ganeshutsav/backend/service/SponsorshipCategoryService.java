package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.SponsorshipDtos.CategoryDTO;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.entity.SponsorshipCategory;
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
public class SponsorshipCategoryService {

    private final SponsorshipCategoryRepository categoryRepository;
    private final GeneralSponsorRepository generalSponsorRepository;

    public List<CategoryDTO> getAll() {
        return categoryRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    // used to populate the "Sponsorship Category" dropdown on the General Sponsors page
    public List<CategoryDTO> getActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO create(CategoryDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("A sponsorship category named '" + dto.getName() + "' already exists");
        }
        SponsorshipCategory category = SponsorshipCategory.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .active(true)
                .createdBy(getCurrentMember())
                .build();
        return toDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        SponsorshipCategory existing = findEntity(id);
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setActive(dto.isActive());
        return toDTO(categoryRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Sponsorship category not found: " + id);
        }
        if (generalSponsorRepository.existsByCategoryId(id)) {
            throw new IllegalStateException(
                    "Cannot delete this category - one or more sponsors are already assigned to it. " +
                    "Mark it inactive instead to hide it from new sponsorships.");
        }
        categoryRepository.deleteById(id);
    }

    private SponsorshipCategory findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sponsorship category not found: " + id));
    }

    private Member getCurrentMember() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
        return (principal instanceof Member) ? (Member) principal : null;
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
