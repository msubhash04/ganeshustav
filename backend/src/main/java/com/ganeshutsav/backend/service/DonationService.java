package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.DonationDTO;
import com.ganeshutsav.backend.entity.Donation;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.DonationRepository;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import com.ganeshutsav.backend.util.ReceiptNumberGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final ReceiptNumberGenerator receiptNumberGenerator;
    private final FestivalYearRepository festivalYearRepository;

    public List<DonationDTO> getAll() {
        return donationRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DonationDTO> search(String name, LocalDate startDate, LocalDate endDate,
                                     BigDecimal minAmount, BigDecimal maxAmount) {
        return donationRepository.search(
                (name == null || name.isBlank()) ? null : name,
                startDate, endDate, minAmount, maxAmount
        ).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public DonationDTO getById(Long id) {
        return toDTO(findEntity(id));
    }

    @Transactional
    public DonationDTO create(DonationDTO dto) {
        Member current = getCurrentMember();
        FestivalYear year = resolveFestivalYear(dto.getFestivalYearId());
        Donation donation = Donation.builder()
                .receiptNumber(receiptNumberGenerator.next())
                .donorName(dto.getDonorName())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .amount(dto.getAmount())
                .paymentMode(dto.getPaymentMode())
                .donationDate(dto.getDonationDate())
                .recordedBy(current)
                .festivalYear(year)
                .build();
        return toDTO(donationRepository.save(donation));
    }

    // uses the explicitly provided festival year, or falls back to whichever
    // year is currently marked active - keeps the API usable even before the
    // frontend is updated to always send a festivalYearId
    private FestivalYear resolveFestivalYear(Long festivalYearId) {
        if (festivalYearId != null) {
            return festivalYearRepository.findById(festivalYearId).orElse(null);
        }
        return festivalYearRepository.findFirstByActiveTrueOrderByIdDesc().orElse(null);
    }

    @Transactional
    public DonationDTO update(Long id, DonationDTO dto) {
        Donation existing = findEntity(id);
        existing.setDonorName(dto.getDonorName());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setAddress(dto.getAddress());
        existing.setAmount(dto.getAmount());
        existing.setPaymentMode(dto.getPaymentMode());
        existing.setDonationDate(dto.getDonationDate());
        return toDTO(donationRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!donationRepository.existsById(id)) {
            throw new EntityNotFoundException("Donation not found: " + id);
        }
        donationRepository.deleteById(id);
    }

    public BigDecimal getTotalCollection() {
        return donationRepository.getTotalCollection();
    }

    private Donation findEntity(Long id) {
        return donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Donation not found: " + id));
    }

    private Member getCurrentMember() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
        return (principal instanceof Member) ? (Member) principal : null;
    }

    private DonationDTO toDTO(Donation d) {
        DonationDTO dto = new DonationDTO();
        dto.setId(d.getId());
        dto.setDonorName(d.getDonorName());
        dto.setPhoneNumber(d.getPhoneNumber());
        dto.setAddress(d.getAddress());
        dto.setAmount(d.getAmount());
        dto.setPaymentMode(d.getPaymentMode());
        dto.setDonationDate(d.getDonationDate());
        dto.setReceiptNumber(d.getReceiptNumber());
        dto.setRecordedByName(d.getRecordedBy() != null ? d.getRecordedBy().getName() : null);
        dto.setFestivalYearId(d.getFestivalYear() != null ? d.getFestivalYear().getId() : null);
        return dto;
    }
}
