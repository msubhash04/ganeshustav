package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.entity.Member;
import com.ganeshutsav.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<Member> getAll() {
        return memberRepository.findAll();
    }

    @PostMapping
    public Member create(@RequestBody Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        member.setActive(true);
        return memberRepository.save(member);
    }

    @PutMapping("/{id}/deactivate")
    public Member deactivate(@PathVariable Long id) {
        Member m = memberRepository.findById(id).orElseThrow();
        m.setActive(false);
        return memberRepository.save(m);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        memberRepository.deleteById(id);
    }
}
