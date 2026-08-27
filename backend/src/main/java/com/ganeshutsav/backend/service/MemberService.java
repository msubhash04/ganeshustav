package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.MemberDtos.CreateStaffRequest;
import com.ganeshutsav.backend.dto.MemberDtos.MemberResponse;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.CommitteeRole;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.MemberRepository;
import com.ganeshutsav.backend.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private static final Set<String> STAFF_ROLES = Set.of("TREASURER", "SECRETARY", "VOLUNTEER");

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantContext tenantContext;

    // President viewing their OWN committee's staff roster only
    public List<MemberResponse> getAll() {
        Long committeeId = tenantContext.requireCommitteeId();
        return memberRepository.findByCommitteeIdOrderByNameAsc(committeeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public MemberResponse createStaff(CreateStaffRequest req) {
        Committee committee = tenantContext.requireCommittee();

        String role = req.getRole().toUpperCase();
        if (!STAFF_ROLES.contains(role)) {
            throw new IllegalArgumentException(
                    "This endpoint can only add TREASURER, SECRETARY, or VOLUNTEER accounts. " +
                    "PRESIDENT accounts are created by the Developer when a committee is set up.");
        }
        if (memberRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        Member member = Member.builder()
                .name(req.getName())
                .phone(req.getPhone())
                .email(normalizeEmail(req.getEmail()))
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(CommitteeRole.valueOf(role))
                .committee(committee) // ALWAYS the caller's own committee - never client-supplied
                .active(true)
                .build();
        return toResponse(memberRepository.save(member));
    }

    // Email is optional but unique at the DB level. MySQL's unique index
    // allows any number of NULLs (they're never considered equal to each
    // other) but does NOT allow duplicate empty strings - so a second
    // member submitted with a blank email field would collide with the
    // first and throw a DataIntegrityViolationException. Treating a
    // blank/empty email as NULL avoids that entirely.
    private String normalizeEmail(String email) {
        return (email == null || email.isBlank()) ? null : email.trim();
    }

    @Transactional
    public MemberResponse deactivate(Long id) {
        Member member = findOwnedEntity(id);
        member.setActive(false);
        return toResponse(memberRepository.save(member));
    }

    @Transactional
    public void delete(Long id) {
        Member member = findOwnedEntity(id);
        if (member.getRole() == CommitteeRole.PRESIDENT) {
            throw new IllegalStateException("A President account cannot be removed from this screen. Contact platform support.");
        }
        memberRepository.deleteById(id);
    }

    // loads by id, then verifies it belongs to the caller's own committee -
    // prevents "President A deactivating Committee B's staff member"
    private Member findOwnedEntity(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + id));
        tenantContext.assertOwnedByCurrentTenant(member.getCommittee());
        return member;
    }

    private MemberResponse toResponse(Member m) {
        return MemberResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .phone(m.getPhone())
                .email(m.getEmail())
                .role(m.getRole())
                .username(m.getUsername())
                .active(m.isActive())
                .build();
    }
}
