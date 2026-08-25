package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.SponsorshipDtos.AnnadanamSponsorDTO;
import com.ganeshutsav.backend.service.AnnadanamSponsorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annadanam-sponsors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRESIDENT')")
public class AnnadanamSponsorController {

    private final AnnadanamSponsorService annadanamSponsorService;

    @GetMapping("/festival-year/{festivalYearId}")
    public List<AnnadanamSponsorDTO> getByFestivalYear(@PathVariable Long festivalYearId) {
        return annadanamSponsorService.getByFestivalYear(festivalYearId);
    }

    @PostMapping
    public ResponseEntity<AnnadanamSponsorDTO> create(@Valid @RequestBody AnnadanamSponsorDTO dto) {
        return ResponseEntity.ok(annadanamSponsorService.create(dto));
    }

    @PutMapping("/{id}")
    public AnnadanamSponsorDTO update(@PathVariable Long id, @Valid @RequestBody AnnadanamSponsorDTO dto) {
        return annadanamSponsorService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        annadanamSponsorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
