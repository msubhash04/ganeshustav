package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.DonationDTO;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.Donation;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.repository.DonationRepository;
import com.ganeshutsav.backend.security.TenantContext;
import com.ganeshutsav.backend.util.ReceiptNumberGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
    private final FestivalYearGuard festivalYearGuard;
    private final TenantContext tenantContext;

    public List<DonationDTO> getAll() {
        Long committeeId = tenantContext.requireCommitteeId();
        return donationRepository.findByCommitteeIdOrderByDonationDateDesc(committeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DonationDTO> search(String name, LocalDate startDate, LocalDate endDate,
                                     BigDecimal minAmount, BigDecimal maxAmount) {
        Long committeeId = tenantContext.requireCommitteeId();
        return donationRepository.search(
                committeeId, (name == null || name.isBlank()) ? null : name,
                startDate, endDate, minAmount, maxAmount
        ).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public DonationDTO getById(Long id) {
        return toDTO(findOwnedEntity(id));
    }

    @Transactional
    public DonationDTO create(DonationDTO dto) {
        Committee committee = tenantContext.requireCommittee();
        // RULE: a new collection can only ever be filed against the
        // currently active festival year - throws with the standard
        // "First create the Festival year..." message if none exists,
        // or if the requested year has since been archived.
        FestivalYear year = festivalYearGuard.resolveForNewRecord(dto.getFestivalYearId());
        Donation donation = Donation.builder()
                .committee(committee)
                .receiptNumber(receiptNumberGenerator.next())
                .donorName(dto.getDonorName())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .amount(dto.getAmount())
                .paymentMode(dto.getPaymentMode())
                .donationDate(dto.getDonationDate())
                .recordedBy(tenantContext.getCurrentMember())
                .festivalYear(year)
                .build();
        return toDTO(donationRepository.save(donation));
    }

    @Transactional
    public DonationDTO update(Long id, DonationDTO dto) {
        Donation existing = findOwnedEntity(id);
        // RULE: a record filed under a since-archived festival year can
        // no longer be modified.
        festivalYearGuard.assertActive(existing.getFestivalYear());
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
        Donation existing = findOwnedEntity(id); // verifies ownership before deleting
        festivalYearGuard.assertActive(existing.getFestivalYear());
        donationRepository.deleteById(id);
    }

    public BigDecimal getTotalCollection() {
        return donationRepository.getTotalCollection(tenantContext.requireCommitteeId());
    }

    // loads by id, then verifies it belongs to the caller's own committee -
    // this is what actually prevents "Committee A reading Committee B's donations"
    private Donation findOwnedEntity(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Donation not found: " + id));
        tenantContext.assertOwnedByCurrentTenant(donation.getCommittee());
        return donation;
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
