package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.AnnadanamSponsor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnadanamSponsorRepository extends JpaRepository<AnnadanamSponsor, Long> {
    List<AnnadanamSponsor> findByFestivalYearIdOrderByDayNumberAsc(Long festivalYearId);
    List<AnnadanamSponsor> findByFestivalYearIdAndDayNumber(Long festivalYearId, Integer dayNumber);
}
