package com.ganeshutsav.backend.util;

import com.ganeshutsav.backend.repository.CommitteeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantCodeGenerator {

    private final CommitteeRepository committeeRepository;

    /**
     * Generates a "Ganesh Unique Code" like GU-MH-PUN-0001. Checks the
     * database for a collision on every attempt (rather than relying on
     * an in-memory counter, unlike ReceiptNumberGenerator) since a
     * duplicate tenant code would be a much more serious problem than a
     * duplicate receipt number - it's the primary key donors and the
     * Developer both use to identify a committee.
     */
    public String generate(String state, String city) {
        String stateCode = shortCode(state, 2);
        String cityCode = shortCode(city, 3);
        String prefix = "GU-" + stateCode + "-" + cityCode + "-";

        for (int seq = 1; seq <= 9999; seq++) {
            String candidate = prefix + String.format("%04d", seq);
            if (!committeeRepository.existsByTenantCode(candidate)) {
                return candidate;
            }
        }
        // extremely unlikely fallback if a single city somehow registers
        // more than 9999 committees
        return prefix + System.currentTimeMillis();
    }

    private String shortCode(String value, int length) {
        String cleaned = value == null ? "XX" : value.trim().toUpperCase().replaceAll("[^A-Z]", "");
        if (cleaned.isEmpty()) cleaned = "XX";
        return cleaned.length() >= length ? cleaned.substring(0, length) : cleaned;
    }
}
