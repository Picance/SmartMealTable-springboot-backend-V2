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
    
    /**
     * 그룹 이름 중복 체크
     */
    boolean existsByName(String name);
    
    /**
     * 그룹 이름 중복 체크 (자기 자신 제외)
     */
    boolean existsByNameAndGroupIdNot(String name, Long groupId);
    
    /**
     * 그룹에 속한 회원 수 조회
     */
    @Query("SELECT COUNT(m) FROM MemberJpaEntity m WHERE m.groupId = :groupId")
    long countMembersByGroupId(@Param("groupId") Long groupId);
    
    // ==================== 검색 기능 강화 메서드 ====================
    
    /**
     * 이름으로 시작하는 그룹 검색 (Prefix 검색)
     * B-Tree 인덱스 활용 가능
     */
    @Query("SELECT g FROM GroupJpaEntity g WHERE g.name LIKE :prefix%")
    List<GroupJpaEntity> findByNameStartingWith(@Param("prefix") String prefix);
    
    /**
     * 여러 ID로 그룹 일괄 조회
     */
    @Query("SELECT g FROM GroupJpaEntity g WHERE g.groupId IN :groupIds")
    List<GroupJpaEntity> findByGroupIdIn(@Param("groupIds") List<Long> groupIds);

    /**
     * 이름에 포함된 키워드로 검색 (이름 길이순으로 정렬)
     * UI/UX 개선: 짧은 이름을 먼저 표시하여 더 많은 정보 노출
     *
     * 예: "과학기술대" 검색 시
     *   1. "서울과학기술대학교" (O, contains)
     *   2. "과학기술대" (O, contains)
     *   등을 이름 길이 짧은 순으로 정렬해서 반환
     */
    @Query("SELECT g FROM GroupJpaEntity g WHERE g.name LIKE %:keyword% ORDER BY CHAR_LENGTH(g.name) ASC")
    List<GroupJpaEntity> findByNameContainingOrderByNameLength(@Param("keyword") String keyword);
}
