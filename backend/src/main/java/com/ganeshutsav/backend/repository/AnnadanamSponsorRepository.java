package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.AnnadanamSponsor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnadanamSponsorRepository extends JpaRepository<AnnadanamSponsor, Long> {
    List<AnnadanamSponsor> findByFestivalYearIdOrderByDayNumberAsc(Long festivalYearId);

    // used by the Festival Archives audit report's category breakdown
    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(a.contributionAmount),0) FROM AnnadanamSponsor a WHERE a.festivalYear.id = :festivalYearId")
    java.math.BigDecimal getTotalContributionByFestivalYear(Long festivalYearId);
}
