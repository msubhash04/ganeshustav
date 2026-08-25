package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.FestivalYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FestivalYearRepository extends JpaRepository<FestivalYear, Long> {

    // NOTE: intentionally NOT "findByActiveTrue()" - that derived query expects
    // exactly one match and throws IncorrectResultSizeDataAccessException if more
    // than one row is ever marked active (e.g. from manual test data or an edge
    // case in the activation flow). This variant always deterministically picks
    // the most recently created active year instead of crashing.
    Optional<FestivalYear> findFirstByActiveTrueOrderByIdDesc();

    // bulk-clears every active flag in one statement, so callers can self-heal
    // any pre-existing "multiple rows marked active" data before setting a new one
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FestivalYear f SET f.active = false WHERE f.active = true")
    void deactivateAll();

    Optional<FestivalYear> findByYear(Integer year);
    boolean existsByYear(Integer year);
}
