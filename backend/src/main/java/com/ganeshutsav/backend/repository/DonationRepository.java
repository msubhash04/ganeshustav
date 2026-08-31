package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByCommitteeIdOrderByDonationDateDesc(Long committeeId);

    List<Donation> findTop10ByCommitteeIdOrderByCreatedAtDesc(Long committeeId);

    List<Donation> findByFestivalYearIdOrderByDonationDateDesc(Long festivalYearId);

    List<Donation> findTop10ByFestivalYearIdOrderByCreatedAtDesc(Long festivalYearId);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.committee.id = :committeeId")
    BigDecimal getTotalCollection(@Param("committeeId") Long committeeId);

    // scoped to a single festival year - used by the Dashboard (active
    // year only) and the Festival Archives audit report (any owned year)
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.festivalYear.id = :festivalYearId")
    BigDecimal getTotalCollectionByFestivalYear(@Param("festivalYearId") Long festivalYearId);

    // MULTI-TENANT SAFETY: committeeId is always required and comes from the
    // authenticated caller's own committee (via TenantContext) - never from
    // client input - so this can never return another committee's donations
    @Query("SELECT d FROM Donation d WHERE d.committee.id = :committeeId AND " +
           "(:name IS NULL OR LOWER(d.donorName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:startDate IS NULL OR d.donationDate >= :startDate) AND " +
           "(:endDate IS NULL OR d.donationDate <= :endDate) AND " +
           "(:minAmount IS NULL OR d.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR d.amount <= :maxAmount) " +
           "ORDER BY d.donationDate DESC")
    List<Donation> search(@Param("committeeId") Long committeeId,
                           @Param("name") String name,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate,
                           @Param("minAmount") BigDecimal minAmount,
                           @Param("maxAmount") BigDecimal maxAmount);

    List<Donation> findByCommitteeIdAndDonationDateBetween(Long committeeId, LocalDate start, LocalDate end);

    long countByCommitteeId(Long committeeId);

    // used by the Developer's global overview dashboard - intentionally NOT
    // committee-scoped, and only ever called from DEVELOPER-gated code paths
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d")
    BigDecimal getTotalCollectionAllCommittees();
}
