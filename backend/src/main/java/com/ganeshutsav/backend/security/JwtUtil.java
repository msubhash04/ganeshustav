package com.ganeshutsav.backend.security;

import com.ganeshutsav.backend.entity.InspectionMode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    // Intentionally has NO real default here - a real secret must never be
    // committed to source control. If JWT_SECRET isn't set, init() below
    // generates a random one for this process only (see the warning it logs).
    @Value("${app.jwt.secret:}")
    private String configuredSecret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.inspection-expiration-ms:1800000}")
    private long inspectionExpirationMs;

    private String secret;

    @PostConstruct
    public void init() {
        if (configuredSecret == null || configuredSecret.isBlank()) {
            secret = Encoders.BASE64.encode(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());
            log.warn("################################################################");
            log.warn("# app.jwt.secret / JWT_SECRET is not set.");
            log.warn("# Generated a random secret for THIS PROCESS ONLY - fine for");
            log.warn("# local development, but every restart invalidates all existing");
            log.warn("# tokens (everyone gets logged out) and it MUST NOT be used in");
            log.warn("# production. Set the JWT_SECRET environment variable, e.g.:");
            log.warn("#   openssl rand -base64 64");
            log.warn("################################################################");
        } else {
            secret = configuredSecret;
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username, String role, String tenantCode) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        var builder = Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry);
        // tenantCode is included ONLY so the frontend can display "which
        // committee am I logged into" without an extra API call. The
        // backend NEVER trusts this claim for authorization decisions -
        // every service re-resolves the tenant from the Member row in
        // the database on every request, via TenantContext. Null for
        // DEVELOPER (Super Admin) accounts, which belong to no committee.
        if (tenantCode != null) {
            builder.claim("tenantCode", tenantCode);
        }
        return builder.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    // Tenant Inspection ("View as President") token. Distinct from a
    // normal login token in two ways: it carries the inspectedCommitteeId
    // and inspectionMode claims that TenantContext and InspectionModeFilter
    // key off of, and it expires much sooner (30 min vs the normal
    // session length) so an elevated-context token never lingers.
    public String generateInspectionToken(String developerUsername, Long committeeId, String tenantCode, InspectionMode mode) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + inspectionExpirationMs);
        return Jwts.builder()
                .subject(developerUsername)
                .claim("role", "DEVELOPER")
                .claim("inspectedCommitteeId", committeeId)
                .claim("inspectedTenantCode", tenantCode)
                .claim("inspectionMode", mode.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public long getInspectionExpirationMs() {
        return inspectionExpirationMs;
    }

    public Long extractInspectedCommitteeId(String token) {
        Object value = extractAllClaims(token).get("inspectedCommitteeId");
        return value == null ? null : ((Number) value).longValue();
    }

    public String extractInspectedTenantCode(String token) {
        return extractAllClaims(token).get("inspectedTenantCode", String.class);
    }

    public InspectionMode extractInspectionMode(String token) {
        String mode = extractAllClaims(token).get("inspectionMode", String.class);
        return mode == null ? null : InspectionMode.valueOf(mode);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
