package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.GeneralSponsor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeneralSponsorRepository extends JpaRepository<GeneralSponsor, Long> {
    List<GeneralSponsor> findByCommitteeIdOrderByCreatedAtDesc(Long committeeId);
    List<GeneralSponsor> findByFestivalYearIdOrderByCreatedAtDesc(Long festivalYearId);
    boolean existsByCategoryId(Long categoryId);

    // used by the Festival Archives audit report's category breakdown
    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(g.contributionAmount),0) FROM GeneralSponsor g WHERE g.festivalYear.id = :festivalYearId")
    java.math.BigDecimal getTotalContributionByFestivalYear(Long festivalYearId);
}
