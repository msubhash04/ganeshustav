package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.AuthDtos.ChangePasswordRequest;
import com.ganeshutsav.backend.dto.AuthDtos.LoginRequest;
import com.ganeshutsav.backend.dto.AuthDtos.LoginResponse;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.MemberRepository;
import com.ganeshutsav.backend.security.JwtUtil;
import com.ganeshutsav.backend.security.TenantContext;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TenantContext tenantContext;

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

    // SECURITY NOTE: public self-registration (previously here) has been
    // removed. It accepted a committee's "Ganesh Unique Code" (tenantCode)
    // as proof of authorization to create a staff login - but that same
    // code is deliberately displayed to the general public on the
    // /public/transparency/{tenantCode} page (see PublicController), so
    // anyone who viewed a committee's donor-transparency page could have
    // self-registered as TREASURER/SECRETARY/VOLUNTEER for that committee
    // and gained full CRUD access to its donations/expenses.
    //
    // Staff accounts are created exclusively by an authenticated PRESIDENT
    // via POST /api/members (see MemberController), which is scoped to the
    // caller's own committee through TenantContext and cannot be spoofed.
    // PRESIDENT accounts are created by the Developer when the committee
    // itself is set up (see CommitteeController), and DEVELOPER accounts
    // are never created through any API endpoint - only via direct DB
    // seeding, by design.

    // Lets any authenticated member change their own password. Works for
    // every role (President, Treasurer, Secretary, Volunteer, Developer),
    // since it operates on "whoever the JWT says is calling", never on a
    // client-supplied member id.
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Member member = tenantContext.getCurrentMember();
        if (member == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            return ResponseEntity.status(400).body("Current password is incorrect");
        }
        if (request.getNewPassword().length() < 8) {
            return ResponseEntity.status(400).body("New password must be at least 8 characters long");
        }
        if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            return ResponseEntity.status(400).body("New password must be different from the current password");
        }

        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);
        return ResponseEntity.ok("Password updated successfully");
    }
}
