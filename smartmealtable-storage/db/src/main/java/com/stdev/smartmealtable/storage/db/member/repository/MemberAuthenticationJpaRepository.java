package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.storage.db.member.entity.MemberAuthenticationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * MemberAuthentication Spring Data JPA Repository
 */
public interface MemberAuthenticationJpaRepository extends JpaRepository<MemberAuthenticationJpaEntity, Long> {

    /**
     * Member ID로 조회
     */
    @Query("SELECT m FROM MemberAuthenticationJpaEntity m WHERE m.memberId = :memberId")
    Optional<MemberAuthenticationJpaEntity> findByMemberId(@Param("memberId") Long memberId);

    /**
     * Email로 조회
     */
    @Query("SELECT m FROM MemberAuthenticationJpaEntity m WHERE m.email = :email")
    Optional<MemberAuthenticationJpaEntity> findByEmail(@Param("email") String email);

    /**
     * Email 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END FROM MemberAuthenticationJpaEntity m WHERE m.email = :email")
    boolean existsByEmail(@Param("email") String email);
}
