package com.ganeshutsav.backend.repository;

import com.ganeshutsav.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);

    // used by JwtAuthFilter, which runs outside any @Transactional boundary -
    // JOIN FETCH loads the committee eagerly in this one query so checking
    // member.getCommittee().isActive() afterward never triggers a
    // LazyInitializationException
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.committee WHERE m.username = :username")
    Optional<Member> findByUsernameWithCommittee(@Param("username") String username);

    java.util.List<Member> findByCommitteeIdOrderByNameAsc(Long committeeId);
}
