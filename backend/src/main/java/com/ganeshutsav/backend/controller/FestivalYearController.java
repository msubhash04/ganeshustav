package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.FestivalYearDTO;
import com.ganeshutsav.backend.service.FestivalYearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/festival-years")
@RequiredArgsConstructor
public class FestivalYearController {

    private final FestivalYearService festivalYearService;

    // All committee members can view festival years (needed for data entry dropdowns)
    @GetMapping
    public List<FestivalYearDTO> getAll() {
        return festivalYearService.getAll();
    }

    @GetMapping("/active")
    public FestivalYearDTO getActive() {
        return festivalYearService.getActive();
    }

    @GetMapping("/{id}")
    public FestivalYearDTO getById(@PathVariable Long id) {
        return festivalYearService.getById(id);
    }

    // Only the President can create a new festival year (with carry-forward balance + duration)
    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    public FestivalYearDTO create(@Valid @RequestBody FestivalYearDTO dto) {
        return festivalYearService.create(dto);
    }

    // Only the President can edit the date / duration / carry-forward later
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public FestivalYearDTO update(@PathVariable Long id, @Valid @RequestBody FestivalYearDTO dto) {
        return festivalYearService.update(id, dto);
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('PRESIDENT')")
    public FestivalYearDTO setActive(@PathVariable Long id) {
        return festivalYearService.setActive(id);
    }
}
