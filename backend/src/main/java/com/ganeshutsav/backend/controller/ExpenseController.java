package com.ganeshutsav.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ganeshutsav.backend.dto.ExpenseDTO;
import com.ganeshutsav.backend.entity.ExpenseCategory;
import com.ganeshutsav.backend.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @GetMapping
    public List<ExpenseDTO> getAll(
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (category != null || startDate != null || endDate != null) {
            return expenseService.search(category, startDate, endDate);
        }
        return expenseService.getAll();
    }

    @GetMapping("/{id}")
    public ExpenseDTO getById(@PathVariable Long id) {
        return expenseService.getById(id);
    }

    // multipart/form-data: "expense" (JSON string) + optional "billFile"
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ExpenseDTO> create(@RequestPart("expense") String expenseJson,
                                              @RequestPart(value = "billFile", required = false) MultipartFile billFile) throws IOException {
        ExpenseDTO dto = objectMapper.readValue(expenseJson, ExpenseDTO.class);
        return ResponseEntity.ok(expenseService.create(dto, billFile));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ExpenseDTO update(@PathVariable Long id,
                              @RequestPart("expense") String expenseJson,
                              @RequestPart(value = "billFile", required = false) MultipartFile billFile) throws IOException {
        ExpenseDTO dto = objectMapper.readValue(expenseJson, ExpenseDTO.class);
        return expenseService.update(id, dto, billFile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total")
    public BigDecimal getTotal() {
        return expenseService.getTotalExpenses();
    }

    @GetMapping("/category-summary")
    public java.util.Map<String, BigDecimal> getCategorySummary() {
        return expenseService.getCategoryWiseTotals();
    }

    @GetMapping("/festival-year/{festivalYearId}")
    public List<ExpenseDTO> getByFestivalYear(@PathVariable Long festivalYearId) {
        return expenseService.getByFestivalYear(festivalYearId);
    }

    // day number -> total spent that day (Day 1, Day 2, ... Grand Total is the sum of these)
    @GetMapping("/festival-year/{festivalYearId}/day-wise-summary")
    public java.util.Map<Integer, BigDecimal> getDayWiseSummary(@PathVariable Long festivalYearId) {
        return expenseService.getDayWiseTotals(festivalYearId);
    }
}
