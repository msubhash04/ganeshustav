package com.ganeshutsav.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Global, cross-committee overview widgets for the Developer (Super Admin)
 * Dashboard. Every figure here intentionally spans ALL tenants - this DTO
 * must never be returned to a President/Member-scoped endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperDashboardDTO {
    private long totalRegisteredCommittees;
    private long activeCommittees;
    private long activeUtsavsThisYear;
    private BigDecimal totalCollectionsAllCommittees;
    private BigDecimal totalExpensesAllCommittees;
    private BigDecimal totalLentMoneyAllCommittees; // sum of outstanding loan principal, all committees
}
