package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.storage.db.member.entity.MemberAuthenticationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * MemberAuthentication Spring Data JPA Repository
 */
public interface MemberAuthenticationJpaRepository extends JpaRepository<MemberAuthenticationJpaEntity, Long> {

    /**
     * Member ID로 조회
     */
    Optional<MemberAuthenticationJpaEntity> findByMemberId(Long memberId);

    /**
     * Email로 조회
     */
    Optional<MemberAuthenticationJpaEntity> findByEmail(String email);

    /**
     * Email 존재 여부 확인
     */
    boolean existsByEmail(String email);
}
