package com.ganeshutsav.backend.security;

import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception ex) {
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // JOIN FETCH loads committee in the same query - safe to call
            // getCommittee() below even though this filter runs outside a
            // Hibernate session/transaction
            Optional<Member> memberOpt = memberRepository.findByUsernameWithCommittee(username);

            if (memberOpt.isPresent() && jwtUtil.isTokenValid(token, username)) {
                Member member = memberOpt.get();

                // reject deactivated accounts even with an otherwise-valid,
                // unexpired token
                if (!member.isActive()) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // reject if this member's committee has been locked by the
                // Developer post-festival (DEVELOPER accounts have no
                // committee and are never affected by this check)
                if (member.getCommittee() != null && !member.getCommittee().isActive()) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String role = jwtUtil.extractRole(token);
                var authToken = new UsernamePasswordAuthenticationToken(
                        member, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
