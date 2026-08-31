package com.ganeshutsav.backend.security;

import com.ganeshutsav.backend.entity.InspectionAuditLog;
import com.ganeshutsav.backend.entity.InspectionMode;
import com.ganeshutsav.backend.repository.InspectionAuditLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Single enforcement point for Tenant Inspection Mode. Registered in
 * SecurityConfig immediately after JwtAuthFilter, so InspectionDetails
 * (see TenantContext) is already resolved before this filter looks at it.
 *
 * Deliberately implemented as a filter rather than per-controller
 * @PreAuthorize annotations: a filter can't be forgotten on a new
 * endpoint the way an annotation can, and it uniformly covers every
 * domain module (Donations, Expenses, Sponsorships, Auction, Loans,
 * Festival Years) without touching any of their controllers.
 */
@Component
@RequiredArgsConstructor
public class InspectionModeFilter extends OncePerRequestFilter {

    private final TenantContext tenantContext;
    private final InspectionAuditLogRepository auditLogRepository;

    // Fully excluded from inspection, EVERY method (including reads), in
    // BOTH modes - committee-level settings (lock/unlock, regenerate
    // code) stay entirely off the table while inspecting.
    private static final String[] FULLY_EXCLUDED_PREFIXES = { "/api/committees" };

    // Excluded from MUTATION only, in BOTH modes - even ADMIN_OVERRIDE,
    // which otherwise grants ROLE_PRESIDENT. A Developer inspecting a
    // committee can observe its staff roster (read), which is why this
    // isn't in FULLY_EXCLUDED_PREFIXES above, but can never add, remove,
    // or deactivate a staff member on that committee's behalf.
    private static final String[] WRITE_EXCLUDED_PREFIXES = { "/api/members" };
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        InspectionDetails inspection = tenantContext.getInspectionDetails();

        if (!inspection.isInspecting()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        boolean isMutating = !SAFE_METHODS.contains(request.getMethod().toUpperCase());

        for (String prefix : FULLY_EXCLUDED_PREFIXES) {
            if (path.startsWith(prefix)) {
                writeForbidden(response, "Committee-level settings stay off-limits during tenant inspection, " +
                        "regardless of mode. Exit inspection to manage this from the Developer Dashboard.");
                return;
            }
        }
        for (String prefix : WRITE_EXCLUDED_PREFIXES) {
            if (path.startsWith(prefix) && isMutating) {
                writeForbidden(response, "Staff management stays off-limits during tenant inspection, " +
                        "regardless of mode. You can still view the committee's staff roster. " +
                        "Exit inspection to add, remove, or deactivate a member.");
                return;
            }
        }

        if (inspection.getMode() == InspectionMode.READ_ONLY && isMutating) {
            writeForbidden(response, "Read-only inspection mode - switch to Admin Override Mode to make changes.");
            return;
        }

        filterChain.doFilter(request, response);

        // Only log successful mutations made under ADMIN_OVERRIDE - a
        // blocked READ_ONLY attempt never reaches here (returned above),
        // and a failed/erroring mutation isn't worth a permanent record.
        if (inspection.getMode() == InspectionMode.ADMIN_OVERRIDE && isMutating
                && response.getStatus() >= 200 && response.getStatus() < 300) {
            var member = tenantContext.getCurrentMember();
            auditLogRepository.save(InspectionAuditLog.builder()
                    .developerId(member.getId())
                    .developerUsername(member.getUsername())
                    .committeeId(inspection.getInspectedCommitteeId())
                    .committeeTenantCode(inspection.getInspectedTenantCode())
                    .mode(inspection.getMode())
                    .eventType("ACTION")
                    .httpMethod(request.getMethod())
                    .path(path)
                    .build());
        }
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message.replace("\"", "'") + "\"}");
    }
}
