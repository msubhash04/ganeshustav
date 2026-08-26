package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.DonationDTO;
import com.ganeshutsav.backend.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @GetMapping
    public List<DonationDTO> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount
    ) {
        if (name != null || startDate != null || endDate != null || minAmount != null || maxAmount != null) {
            return donationService.search(name, startDate, endDate, minAmount, maxAmount);
        }
        return donationService.getAll();
    }

    @GetMapping("/{id}")
    public DonationDTO getById(@PathVariable Long id) {
        return donationService.getById(id);
    }

    @PostMapping
    public ResponseEntity<DonationDTO> create(@Valid @RequestBody DonationDTO dto) {
        return ResponseEntity.ok(donationService.create(dto));
    }

    @PutMapping("/{id}")
    public DonationDTO update(@PathVariable Long id, @Valid @RequestBody DonationDTO dto) {
        return donationService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        donationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total")
    public BigDecimal getTotal() {
        return donationService.getTotalCollection();
    }
}
