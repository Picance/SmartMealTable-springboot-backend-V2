package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.storage.db.member.entity.GroupJpaEntity;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Group Spring Data JPA Repository
 */
public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, Long> {

    /**
     * Type으로 조회
     */
    @Query("SELECT g FROM GroupJpaEntity g WHERE g.type = :type")
    List<GroupJpaEntity> findByType(@Param("type") GroupType type);

    /**
     * 이름으로 검색
     */
    @Query("SELECT g FROM GroupJpaEntity g WHERE g.name LIKE %:name%")
    List<GroupJpaEntity> findByNameContaining(@Param("name") String name);

    /**
     * 타입과 이름으로 검색 (페이징)
     */
    @Query("SELECT g FROM GroupJpaEntity g " +
            "WHERE (:type IS NULL OR g.type = :type) " +
            "AND (:name IS NULL OR g.name LIKE %:name%)")
    Page<GroupJpaEntity> searchGroups(
            @Param("type") GroupType type,
            @Param("name") String name,
            Pageable pageable
    );
}
