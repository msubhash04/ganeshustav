package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.AuctionItemDTO;
import com.ganeshutsav.backend.service.AuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/auction-items")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @GetMapping("/festival-year/{festivalYearId}")
    public List<AuctionItemDTO> getByFestivalYear(@PathVariable Long festivalYearId) {
        return auctionService.getByFestivalYear(festivalYearId);
    }

    @GetMapping("/festival-year/{festivalYearId}/total")
    public BigDecimal getTotal(@PathVariable Long festivalYearId) {
        return auctionService.getTotalForFestivalYear(festivalYearId);
    }

    @PostMapping("/festival-year/{festivalYearId}")
    public ResponseEntity<AuctionItemDTO> create(@PathVariable Long festivalYearId, @Valid @RequestBody AuctionItemDTO dto) {
        return ResponseEntity.ok(auctionService.create(festivalYearId, dto));
    }

    @PutMapping("/{id}")
    public AuctionItemDTO update(@PathVariable Long id, @Valid @RequestBody AuctionItemDTO dto) {
        return auctionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        auctionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
