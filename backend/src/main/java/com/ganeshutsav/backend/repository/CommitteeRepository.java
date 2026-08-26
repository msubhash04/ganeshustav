package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.Committee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommitteeRepository extends JpaRepository<Committee, Long> {

    Optional<Committee> findByTenantCode(String tenantCode);
    boolean existsByTenantCode(String tenantCode);

    // Committee Directory search - Developer-only, filters by city/state/name
    @Query("SELECT c FROM Committee c WHERE " +
           "(:query IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "  OR LOWER(c.tenantCode) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:city IS NULL OR LOWER(c.city) = LOWER(:city)) AND " +
           "(:state IS NULL OR LOWER(c.state) = LOWER(:state)) " +
           "ORDER BY c.createdAt DESC")
    List<Committee> search(@Param("query") String query, @Param("city") String city, @Param("state") String state);

    long countByActiveTrue();
}
