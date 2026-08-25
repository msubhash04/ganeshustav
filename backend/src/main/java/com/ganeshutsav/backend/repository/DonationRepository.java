package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d")
    BigDecimal getTotalCollection();

    @Query("SELECT d FROM Donation d WHERE " +
           "(:name IS NULL OR LOWER(d.donorName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:startDate IS NULL OR d.donationDate >= :startDate) AND " +
           "(:endDate IS NULL OR d.donationDate <= :endDate) AND " +
           "(:minAmount IS NULL OR d.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR d.amount <= :maxAmount) " +
           "ORDER BY d.donationDate DESC")
    List<Donation> search(@Param("name") String name,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate,
                           @Param("minAmount") BigDecimal minAmount,
                           @Param("maxAmount") BigDecimal maxAmount);

    List<Donation> findByDonationDateBetween(LocalDate start, LocalDate end);
}
