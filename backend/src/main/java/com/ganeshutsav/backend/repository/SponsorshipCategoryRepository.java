package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.SponsorshipCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SponsorshipCategoryRepository extends JpaRepository<SponsorshipCategory, Long> {
    List<SponsorshipCategory> findByActiveTrueOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}
