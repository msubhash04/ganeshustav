package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.AuctionItemDTO;
import com.ganeshutsav.backend.entity.AuctionItem;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.AuctionItemRepository;
import com.ganeshutsav.backend.repository.FestivalYearRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public List<AuctionItemDTO> getByFestivalYear(Long festivalYearId) {
        return auctionItemRepository.findByFestivalYearIdOrderByDayNumberAsc(festivalYearId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public BigDecimal getTotalForFestivalYear(Long festivalYearId) {
        return auctionItemRepository.getTotalAuctionAmount(festivalYearId);
    }

    @Transactional
    public AuctionItemDTO create(Long festivalYearId, AuctionItemDTO dto) {
        FestivalYear year = festivalYearRepository.findById(festivalYearId)
                .orElseThrow(() -> new EntityNotFoundException("Festival year not found: " + festivalYearId));

        AuctionItem item = AuctionItem.builder()
                .festivalYear(year)
                .dayNumber(dto.getDayNumber())
                .itemName(dto.getItemName())
                .winnerName(dto.getWinnerName())
                .bidAmount(dto.getBidAmount())
                .paymentStatus(dto.getPaymentStatus())
                .paymentMode(dto.getPaymentMode())
                .recordedBy(getCurrentMember())
                .build();
        return toDTO(auctionItemRepository.save(item));
    }

    @Transactional
    public AuctionItemDTO update(Long id, AuctionItemDTO dto) {
        AuctionItem item = findEntity(id);
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
        if (!auctionItemRepository.existsById(id)) {
            throw new EntityNotFoundException("Auction item not found: " + id);
        }
        auctionItemRepository.deleteById(id);
    }

    private AuctionItem findEntity(Long id) {
        return auctionItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Auction item not found: " + id));
    }

    private Member getCurrentMember() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
        return (principal instanceof Member) ? (Member) principal : null;
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
