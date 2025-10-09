package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.storage.db.member.entity.GroupJpaEntity;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Group Spring Data JPA Repository
 */
public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, Long> {

    /**
     * Type으로 조회
     */
    List<GroupJpaEntity> findByType(GroupType type);

    /**
     * 이름으로 검색
     */
    List<GroupJpaEntity> findByNameContaining(String name);
}
