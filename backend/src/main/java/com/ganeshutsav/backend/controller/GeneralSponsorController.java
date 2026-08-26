package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.SponsorshipDtos.GeneralSponsorDTO;
import com.ganeshutsav.backend.service.GeneralSponsorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/general-sponsors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRESIDENT')")
public class GeneralSponsorController {

    private final GeneralSponsorService generalSponsorService;

    @GetMapping
    public List<GeneralSponsorDTO> getAll() {
        return generalSponsorService.getAll();
    }

    @GetMapping("/festival-year/{festivalYearId}")
    public List<GeneralSponsorDTO> getByFestivalYear(@PathVariable Long festivalYearId) {
        return generalSponsorService.getByFestivalYear(festivalYearId);
    }

    @PostMapping
    public ResponseEntity<GeneralSponsorDTO> create(@Valid @RequestBody GeneralSponsorDTO dto) {
        return ResponseEntity.ok(generalSponsorService.create(dto));
    }

    @PutMapping("/{id}")
    public GeneralSponsorDTO update(@PathVariable Long id, @Valid @RequestBody GeneralSponsorDTO dto) {
        return generalSponsorService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        generalSponsorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
