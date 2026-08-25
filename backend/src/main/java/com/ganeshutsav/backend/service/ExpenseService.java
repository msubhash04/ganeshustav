package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.ExpenseDTO;
import com.ganeshutsav.backend.entity.Expense;
import com.ganeshutsav.backend.entity.ExpenseCategory;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.ExpenseRepository;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final FestivalYearRepository festivalYearRepository;
    private static final String UPLOAD_DIR = "uploads/bills";

    public List<ExpenseDTO> getAll() {
        return expenseRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ExpenseDTO> search(ExpenseCategory category, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.search(category, startDate, endDate)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ExpenseDTO getById(Long id) {
        return toDTO(findEntity(id));
    }

    @Transactional
    public ExpenseDTO create(ExpenseDTO dto, MultipartFile billFile) {
        validateNoteForMiscellaneous(dto);
        Member current = getCurrentMember();
        String filePath = storeFileIfPresent(billFile);
        FestivalYear year = resolveFestivalYear(dto.getFestivalYearId());

        Expense expense = Expense.builder()
                .description(dto.getDescription())
                .category(dto.getCategory())
                .amount(dto.getAmount())
                .paidTo(dto.getPaidTo())
                .expenseDate(dto.getExpenseDate())
                .paymentMode(dto.getPaymentMode())
                .billFilePath(filePath)
                .recordedBy(current)
                .festivalYear(year)
                .dayNumber(dto.getDayNumber())
                .note(dto.getNote())
                .build();
        return toDTO(expenseRepository.save(expense));
    }

    // Gift Distribution / Others (MISCELLANEOUS) requires a note explaining
    // why the amount was spent, per the spec ("give Note for details")
    private void validateNoteForMiscellaneous(ExpenseDTO dto) {
        if (dto.getCategory() == ExpenseCategory.MISCELLANEOUS
                && (dto.getNote() == null || dto.getNote().isBlank())) {
            throw new IllegalArgumentException(
                    "A note explaining the expense is required for the 'Miscellaneous / Gift Distribution' category");
        }
    }

    private FestivalYear resolveFestivalYear(Long festivalYearId) {
        if (festivalYearId != null) {
            return festivalYearRepository.findById(festivalYearId).orElse(null);
        }
        return festivalYearRepository.findFirstByActiveTrueOrderByIdDesc().orElse(null);
    }

    @Transactional
    public ExpenseDTO update(Long id, ExpenseDTO dto, MultipartFile billFile) {
        validateNoteForMiscellaneous(dto);
        Expense existing = findEntity(id);
        existing.setDescription(dto.getDescription());
        existing.setCategory(dto.getCategory());
        existing.setAmount(dto.getAmount());
        existing.setPaidTo(dto.getPaidTo());
        existing.setExpenseDate(dto.getExpenseDate());
        existing.setPaymentMode(dto.getPaymentMode());
        existing.setDayNumber(dto.getDayNumber());
        existing.setNote(dto.getNote());
        if (billFile != null && !billFile.isEmpty()) {
            existing.setBillFilePath(storeFileIfPresent(billFile));
        }
        return toDTO(expenseRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new EntityNotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    public BigDecimal getTotalExpenses() {
        return expenseRepository.getTotalExpenses();
    }

    public List<ExpenseDTO> getByFestivalYear(Long festivalYearId) {
        return expenseRepository.findByFestivalYearIdOrderByDayNumberAsc(festivalYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // day number -> total spent that day, for the "Day 1, Day 2, ..." expense sheets
    public Map<Integer, BigDecimal> getDayWiseTotals(Long festivalYearId) {
        Map<Integer, BigDecimal> result = new LinkedHashMap<>();
        expenseRepository.getDayWiseTotals(festivalYearId)
                .forEach(row -> result.put(row.getDayNumber(), row.getTotal()));
        return result;
    }

    public Map<String, BigDecimal> getCategoryWiseTotals() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        expenseRepository.getCategoryWiseTotals()
                .forEach(row -> result.put(row.getCategory().getLabel(), row.getTotal()));
        return result;
    }

    private String storeFileIfPresent(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            Path dir = Paths.get(UPLOAD_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target);
            return target.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store bill file", e);
        }
    }

    private Expense findEntity(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
    }

    private Member getCurrentMember() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
        return (principal instanceof Member) ? (Member) principal : null;
    }

    private ExpenseDTO toDTO(Expense e) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(e.getId());
        dto.setDescription(e.getDescription());
        dto.setCategory(e.getCategory());
        dto.setAmount(e.getAmount());
        dto.setPaidTo(e.getPaidTo());
        dto.setExpenseDate(e.getExpenseDate());
        dto.setPaymentMode(e.getPaymentMode());
        dto.setBillFilePath(e.getBillFilePath());
        dto.setRecordedByName(e.getRecordedBy() != null ? e.getRecordedBy().getName() : null);
        dto.setApprovedByName(e.getApprovedBy() != null ? e.getApprovedBy().getName() : null);
        dto.setFestivalYearId(e.getFestivalYear() != null ? e.getFestivalYear().getId() : null);
        dto.setDayNumber(e.getDayNumber());
        dto.setNote(e.getNote());
        return dto;
    }
}
