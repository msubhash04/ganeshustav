package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.AuctionItemDTO;
import com.ganeshutsav.backend.entity.AuctionItem;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.repository.AuctionItemRepository;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import com.ganeshutsav.backend.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionItemRepository auctionItemRepository;
    private final FestivalYearRepository festivalYearRepository;
    private final FestivalYearGuard festivalYearGuard;
    private final TenantContext tenantContext;

    public List<AuctionItemDTO> getByFestivalYear(Long festivalYearId) {
        FestivalYear year = findOwnedFestivalYear(festivalYearId);
        return auctionItemRepository.findByFestivalYearIdOrderByDayNumberAsc(year.getId())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public BigDecimal getTotalForFestivalYear(Long festivalYearId) {
        FestivalYear year = findOwnedFestivalYear(festivalYearId);
        return auctionItemRepository.getTotalAuctionAmount(year.getId());
    }

    @Transactional
    public AuctionItemDTO create(Long festivalYearId, AuctionItemDTO dto) {
        // RULE: a new auction item can only ever be filed against the
        // currently active festival year, whether or not one was
        // explicitly requested (an archived year is rejected either way).
        FestivalYear year = festivalYearGuard.resolveForNewRecord(festivalYearId);
        Committee committee = tenantContext.requireCommittee();

        AuctionItem item = AuctionItem.builder()
                .committee(committee)
                .festivalYear(year)
                .dayNumber(dto.getDayNumber())
                .itemName(dto.getItemName())
                .winnerName(dto.getWinnerName())
                .bidAmount(dto.getBidAmount())
                .paymentStatus(dto.getPaymentStatus())
                .paymentMode(dto.getPaymentMode())
                .recordedBy(tenantContext.getCurrentMember())
                .build();
        return toDTO(auctionItemRepository.save(item));
    }

    @Transactional
    public AuctionItemDTO update(Long id, AuctionItemDTO dto) {
        AuctionItem item = findOwnedEntity(id);
        // RULE: a record filed under a since-archived festival year can
        // no longer be modified.
        festivalYearGuard.assertActive(item.getFestivalYear());
        item.setDayNumber(dto.getDayNumber());
        item.setItemName(dto.getItemName());
        item.setWinnerName(dto.getWinnerName());
        item.setBidAmount(dto.getBidAmount());
        item.setPaymentStatus(dto.getPaymentStatus());
        item.setPaymentMode(dto.getPaymentMode());
        return toDTO(auctionItemRepository.save(item));
    }

    @Transactional
    public void delete(Long id) {
        AuctionItem item = findOwnedEntity(id); // verifies ownership before deleting
        festivalYearGuard.assertActive(item.getFestivalYear());
        auctionItemRepository.deleteById(id);
    }

    // guards against a festivalYearId belonging to a different committee
    private FestivalYear findOwnedFestivalYear(Long festivalYearId) {
        FestivalYear year = festivalYearRepository.findById(festivalYearId)
                .orElseThrow(() -> new EntityNotFoundException("Festival year not found: " + festivalYearId));
        tenantContext.assertOwnedByCurrentTenant(year.getCommittee());
        return year;
    }

    private AuctionItem findOwnedEntity(Long id) {
        AuctionItem item = auctionItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Auction item not found: " + id));
        tenantContext.assertOwnedByCurrentTenant(item.getCommittee());
        return item;
    }

    private AuctionItemDTO toDTO(AuctionItem a) {
        AuctionItemDTO dto = new AuctionItemDTO();
        dto.setId(a.getId());
        dto.setDayNumber(a.getDayNumber());
        dto.setItemName(a.getItemName());
        dto.setWinnerName(a.getWinnerName());
        dto.setBidAmount(a.getBidAmount());
        dto.setPaymentStatus(a.getPaymentStatus());
        dto.setPaymentMode(a.getPaymentMode());
        dto.setRecordedByName(a.getRecordedBy() != null ? a.getRecordedBy().getName() : null);
        return dto;
    }
}
