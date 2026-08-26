package com.ganeshutsav.backend.service;

import com.ganeshutsav.backend.dto.CommitteeDtos.*;
import com.ganeshutsav.backend.entity.Committee;
import com.ganeshutsav.backend.entity.CommitteeRole;
import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.CommitteeRepository;
import com.ganeshutsav.backend.repository.MemberRepository;
import com.ganeshutsav.backend.security.TenantContext;
import com.ganeshutsav.backend.util.TenantCodeGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Developer (Super Admin)-only tenant management. Every method here is
 * intentionally NOT committee-scoped - the whole point of this service is
 * cross-committee administration. Access is restricted at the controller
 * layer via @PreAuthorize("hasRole('DEVELOPER')"), not here.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommitteeService {

    private final CommitteeRepository committeeRepository;
    private final MemberRepository memberRepository;
    private final TenantCodeGenerator tenantCodeGenerator;
    private final PasswordEncoder passwordEncoder;
    private final TenantContext tenantContext;

    public List<CommitteeResponse> search(String query, String city, String state) {
        return committeeRepository.search(query, city, state).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CommitteeResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    public long getTotalCommitteeCount() {
        return committeeRepository.count();
    }

    public long getActiveCommitteeCount() {
        return committeeRepository.countByActiveTrue();
    }

    @Transactional
    public CommitteeResponse createCommitteeWithPresident(CreateCommitteeRequest req) {
        if (memberRepository.existsByUsername(req.getPresidentUsername())) {
            throw new IllegalArgumentException("Username '" + req.getPresidentUsername() + "' is already taken");
        }

        String tenantCode = tenantCodeGenerator.generate(req.getState(), req.getCity());

        Committee committee = Committee.builder()
                .tenantCode(tenantCode)
                .name(req.getName())
                .city(req.getCity())
                .state(req.getState())
                .address(req.getAddress())
                .active(true)
                .createdByDeveloper(tenantContext.getCurrentMember())
                .build();
        committee = committeeRepository.save(committee);

        Member president = Member.builder()
                .name(req.getPresidentName())
                .phone(req.getPresidentPhone())
                .username(req.getPresidentUsername())
                .password(passwordEncoder.encode(req.getPresidentPassword()))
                .role(CommitteeRole.PRESIDENT)
                .committee(committee)
                .active(true)
                .build();
        memberRepository.save(president);

        return toResponse(committee);
    }

    @Transactional
    public CommitteeResponse update(Long id, UpdateCommitteeRequest req) {
        Committee committee = findEntity(id);
        committee.setName(req.getName());
        committee.setCity(req.getCity());
        committee.setState(req.getState());
        committee.setAddress(req.getAddress());
        return toResponse(committeeRepository.save(committee));
    }

    @Transactional
    public CommitteeResponse regenerateTenantCode(Long id) {
        Committee committee = findEntity(id);
        committee.setTenantCode(tenantCodeGenerator.generate(committee.getState(), committee.getCity()));
        return toResponse(committeeRepository.save(committee));
    }

    @Transactional
    public CommitteeResponse setLocked(Long id, boolean locked) {
        Committee committee = findEntity(id);
        committee.setActive(!locked);
        return toResponse(committeeRepository.save(committee));
    }

    private Committee findEntity(Long id) {
        return committeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Committee not found: " + id));
    }

    private CommitteeResponse toResponse(Committee c) {
        return CommitteeResponse.builder()
                .id(c.getId())
                .tenantCode(c.getTenantCode())
                .name(c.getName())
                .city(c.getCity())
                .state(c.getState())
                .address(c.getAddress())
                .active(c.isActive())
                .createdAt(c.getCreatedAt())
                .memberCount((long) memberRepository.findByCommitteeIdOrderByNameAsc(c.getId()).size())
                .build();
    }
}
