package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.storage.db.member.entity.MemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Member Spring Data JPA Repository
 */
public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {

    /**
     * 닉네임 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END FROM MemberJpaEntity m WHERE m.nickname = :nickname")
    boolean existsByNickname(@Param("nickname") String nickname);
    
    /**
     * 닉네임 존재 여부 확인 (특정 회원 제외)
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END FROM MemberJpaEntity m WHERE m.nickname = :nickname AND m.id != :memberId")
    boolean existsByNicknameAndMemberIdNot(@Param("nickname") String nickname, @Param("memberId") Long memberId);
}
