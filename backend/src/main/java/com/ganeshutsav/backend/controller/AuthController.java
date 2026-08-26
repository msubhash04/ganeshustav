package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.AuthDtos.LoginRequest;
import com.ganeshutsav.backend.dto.AuthDtos.LoginResponse;
import com.ganeshutsav.backend.dto.AuthDtos.RegisterRequest;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.CommitteeRole;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.CommitteeRepository;
import com.ganeshutsav.backend.repository.MemberRepository;
import com.ganeshutsav.backend.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberRepository memberRepository;
    private final CommitteeRepository committeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // JOIN FETCH loads committee eagerly - safe to read member.getCommittee()
        // below even though this controller method isn't wrapped in @Transactional
        Member member = memberRepository.findByUsernameWithCommittee(request.getUsername())
                .orElse(null);

        if (member == null || !member.isActive() || !passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        Committee committee = member.getCommittee();
        if (committee != null && !committee.isActive()) {
            return ResponseEntity.status(403).body("This committee's access has been locked. Contact the platform administrator.");
        }

        String token = jwtUtil.generateToken(member.getUsername(), member.getRole().name(),
                committee != null ? committee.getTenantCode() : null);

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .memberId(member.getId())
                .name(member.getName())
                .username(member.getUsername())
                .role(member.getRole())
                .committeeId(committee != null ? committee.getId() : null)
                .committeeName(committee != null ? committee.getName() : null)
                .tenantCode(committee != null ? committee.getTenantCode() : null)
                .build();
        return ResponseEntity.ok(response);
    }

    // Self-service registration for STAFF joining an EXISTING committee only.
    // PRESIDENT accounts are created exclusively by the Developer alongside
    // the committee itself (see CommitteeController), and DEVELOPER accounts
    // are never created through any public endpoint - only via direct DB
    // seeding, by design.
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        String requestedRole = request.getRole().toUpperCase();
        if (requestedRole.equals("PRESIDENT") || requestedRole.equals("DEVELOPER")) {
            return ResponseEntity.badRequest().body(
                    "PRESIDENT accounts are created by the Developer when the committee is set up. " +
                    "DEVELOPER accounts cannot be self-registered.");
        }

        if (memberRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        Committee committee = committeeRepository.findByTenantCode(request.getTenantCode()).orElse(null);
        if (committee == null) {
            return ResponseEntity.badRequest().body("Invalid Ganesh Unique Code - no committee found with this code");
        }
        if (!committee.isActive()) {
            return ResponseEntity.badRequest().body("This committee's access has been locked and cannot accept new members");
        }

        Member member = Member.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(CommitteeRole.valueOf(requestedRole))
                .committee(committee)
                .active(true)
                .build();
        memberRepository.save(member);
        return ResponseEntity.ok("Committee member registered successfully");
    }
}
