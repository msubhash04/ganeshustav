package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.ExpenseDTO;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.Expense;
import com.ganeshutsav.backend.entity.ExpenseCategory;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.repository.ExpenseRepository;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import com.ganeshutsav.backend.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
    private final TenantContext tenantContext;
    private static final String UPLOAD_DIR = "uploads/bills";

    public List<ExpenseDTO> getAll() {
        Long committeeId = tenantContext.requireCommitteeId();
        return expenseRepository.findByCommitteeIdOrderByExpenseDateDesc(committeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ExpenseDTO> search(ExpenseCategory category, LocalDate startDate, LocalDate endDate) {
        Long committeeId = tenantContext.requireCommitteeId();
        return expenseRepository.search(committeeId, category, startDate, endDate)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ExpenseDTO getById(Long id) {
        return toDTO(findOwnedEntity(id));
    }

    @Transactional
    public ExpenseDTO create(ExpenseDTO dto, MultipartFile billFile) {
        validateNoteForMiscellaneous(dto);
        Committee committee = tenantContext.requireCommittee();
        String filePath = storeFileIfPresent(billFile);
        FestivalYear year = resolveFestivalYear(dto.getFestivalYearId(), committee.getId());

        Expense expense = Expense.builder()
                .committee(committee)
                .description(dto.getDescription())
                .category(dto.getCategory())
                .amount(dto.getAmount())
                .paidTo(dto.getPaidTo())
                .expenseDate(dto.getExpenseDate())
                .paymentMode(dto.getPaymentMode())
                .billFilePath(filePath)
                .recordedBy(tenantContext.getCurrentMember())
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

    private FestivalYear resolveFestivalYear(Long festivalYearId, Long committeeId) {
        if (festivalYearId != null) {
            return festivalYearRepository.findById(festivalYearId)
                    .filter(y -> y.getCommittee().getId().equals(committeeId))
                    .orElse(null);
        }
        return festivalYearRepository.findFirstByCommitteeIdAndActiveTrueOrderByIdDesc(committeeId).orElse(null);
    }

    @Transactional
    public ExpenseDTO update(Long id, ExpenseDTO dto, MultipartFile billFile) {
        validateNoteForMiscellaneous(dto);
        Expense existing = findOwnedEntity(id);
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
        findOwnedEntity(id); // verifies ownership before deleting
        expenseRepository.deleteById(id);
    }

    public BigDecimal getTotalExpenses() {
        return expenseRepository.getTotalExpenses(tenantContext.requireCommitteeId());
    }

    public List<ExpenseDTO> getByFestivalYear(Long festivalYearId) {
        assertFestivalYearOwnedByCurrentTenant(festivalYearId);
        return expenseRepository.findByFestivalYearIdOrderByDayNumberAsc(festivalYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // day number -> total spent that day, for the "Day 1, Day 2, ..." expense sheets
    public Map<Integer, BigDecimal> getDayWiseTotals(Long festivalYearId) {
        assertFestivalYearOwnedByCurrentTenant(festivalYearId);
        Map<Integer, BigDecimal> result = new LinkedHashMap<>();
        expenseRepository.getDayWiseTotals(festivalYearId)
                .forEach(row -> result.put(row.getDayNumber(), row.getTotal()));
        return result;
    }

    public Map<String, BigDecimal> getCategoryWiseTotals() {
        Long committeeId = tenantContext.requireCommitteeId();
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        expenseRepository.getCategoryWiseTotals(committeeId)
                .forEach(row -> result.put(row.getCategory().getLabel(), row.getTotal()));
        return result;
    }

    // guards against someone passing an arbitrary festivalYearId that
    // belongs to a DIFFERENT committee - without this, day-wise totals for
    // another committee's festival year could leak through these endpoints
    private void assertFestivalYearOwnedByCurrentTenant(Long festivalYearId) {
        FestivalYear year = festivalYearRepository.findById(festivalYearId)
                .orElseThrow(() -> new EntityNotFoundException("Festival year not found: " + festivalYearId));
        tenantContext.assertOwnedByCurrentTenant(year.getCommittee());
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

    // loads by id, then verifies it belongs to the caller's own committee
    private Expense findOwnedEntity(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
        tenantContext.assertOwnedByCurrentTenant(expense.getCommittee());
        return expense;
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
