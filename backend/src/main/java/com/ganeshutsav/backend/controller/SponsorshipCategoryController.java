package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.SponsorshipDtos.CategoryDTO;
import com.ganeshutsav.backend.service.SponsorshipCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sponsorship Categories master page (Section A). President-only, per the
 * "Sponsorship Management (President Access)" module definition.
 */
@RestController
@RequestMapping("/api/sponsorship-categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRESIDENT')")
public class SponsorshipCategoryController {

    private final SponsorshipCategoryService categoryService;

    @GetMapping
    public List<CategoryDTO> getAll() {
        return categoryService.getAll();
    }

    // powers the dynamic category dropdown on the General Sponsors page
    @GetMapping("/active")
    public List<CategoryDTO> getActive() {
        return categoryService.getActive();
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> create(@Valid @RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(categoryService.create(dto));
    }

    @PutMapping("/{id}")
    public CategoryDTO update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        return categoryService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
