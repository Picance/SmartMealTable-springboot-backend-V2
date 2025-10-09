package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.storage.db.member.entity.MemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Member Spring Data JPA Repository
 */
public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {

    /**
     * 닉네임 존재 여부 확인
     */
    boolean existsByNickname(String nickname);
}
