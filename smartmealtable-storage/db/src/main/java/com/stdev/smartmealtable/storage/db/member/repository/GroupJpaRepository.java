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
     * Full-Text Search를 사용한 그룹 이름 검색
     * MySQL의 FULLTEXT INDEX와 ngram parser를 활용하여 한국어 검색 성능을 최적화합니다.
     * 
     * @param name 검색 키워드
     * @return 관련성 점수가 높은 순서로 정렬된 그룹 목록
     */
    @Query(value = """
            SELECT g.* 
            FROM member_group g
            WHERE MATCH(g.name) AGAINST(:name IN NATURAL LANGUAGE MODE)
            ORDER BY MATCH(g.name) AGAINST(:name IN NATURAL LANGUAGE MODE) DESC
            """,
            nativeQuery = true)
    List<GroupJpaEntity> findByNameContaining(@Param("name") String name);

    /**
     * 타입과 이름으로 검색 (Full-Text Search, 페이징)
     * Full-Text Search를 활용하여 한국어 그룹명 검색 성능을 향상시킵니다.
     */
    @Query(value = """
            SELECT g.* 
            FROM member_group g
            WHERE (:type IS NULL OR g.type = :type)
              AND (:name IS NULL OR MATCH(g.name) AGAINST(:name IN NATURAL LANGUAGE MODE))
            ORDER BY 
                CASE WHEN :name IS NOT NULL 
                     THEN MATCH(g.name) AGAINST(:name IN NATURAL LANGUAGE MODE) 
                     ELSE 0 
                END DESC,
                g.name ASC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM member_group g
            WHERE (:type IS NULL OR g.type = :type)
              AND (:name IS NULL OR MATCH(g.name) AGAINST(:name IN NATURAL LANGUAGE MODE))
            """,
            nativeQuery = true)
    Page<GroupJpaEntity> searchGroups(
            @Param("type") String type,
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
}
