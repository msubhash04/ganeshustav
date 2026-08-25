package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.AuthDtos.LoginRequest;
import com.ganeshutsav.backend.dto.AuthDtos.LoginResponse;
import com.ganeshutsav.backend.dto.AuthDtos.RegisterRequest;
import com.ganeshutsav.backend.entity.CommitteeRole;
import com.ganeshutsav.backend.entity.Member;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (member == null || !member.isActive() || !passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        String token = jwtUtil.generateToken(member.getUsername(), member.getRole().name());
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .memberId(member.getId())
                .name(member.getName())
                .username(member.getUsername())
                .role(member.getRole())
                .build();
        return ResponseEntity.ok(response);
    }

    // Registration is intentionally open only for bootstrapping the very first
    // committee account. In production, gate this behind an existing PRESIDENT/TREASURER login
    // or a one-time setup flag.
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }
        Member member = Member.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(CommitteeRole.valueOf(request.getRole().toUpperCase()))
                .active(true)
                .build();
        memberRepository.save(member);
        return ResponseEntity.ok("Committee member registered successfully");
    }
}
