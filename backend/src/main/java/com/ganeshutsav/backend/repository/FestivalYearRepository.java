package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.FestivalYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FestivalYearRepository extends JpaRepository<FestivalYear, Long> {

    List<FestivalYear> findByCommitteeIdOrderByYearDesc(Long committeeId);

    // NOTE: intentionally NOT "findByCommitteeIdAndActiveTrue()" - a derived
    // query expecting exactly one match throws IncorrectResultSizeDataAccessException
    // if more than one row is ever marked active within a committee. This
    // variant always deterministically picks one instead of crashing.
    Optional<FestivalYear> findFirstByCommitteeIdAndActiveTrueOrderByIdDesc(Long committeeId);

    // MULTI-TENANT SAFETY: scoped to a single committee - clearing active
    // flags in one committee must NEVER affect any other committee's data
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FestivalYear f SET f.active = false WHERE f.committee.id = :committeeId AND f.active = true")
    void deactivateAllForCommittee(@Param("committeeId") Long committeeId);

    boolean existsByCommitteeIdAndYear(Long committeeId, Integer year);

    @Query("SELECT COUNT(f) FROM FestivalYear f WHERE f.year = :year AND f.active = true")
    long countActiveUtsavsForYear(@Param("year") Integer year);
}
