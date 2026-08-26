package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.AuctionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {
    List<AuctionItem> findByFestivalYearIdOrderByDayNumberAsc(Long festivalYearId);
    List<AuctionItem> findByFestivalYearIdAndDayNumber(Long festivalYearId, Integer dayNumber);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(a.bidAmount),0) FROM AuctionItem a WHERE a.festivalYear.id = :festivalYearId")
    BigDecimal getTotalAuctionAmount(Long festivalYearId);
}
