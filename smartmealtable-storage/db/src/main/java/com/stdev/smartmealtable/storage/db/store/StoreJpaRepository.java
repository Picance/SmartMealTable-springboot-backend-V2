package com.stdev.smartmealtable.storage.db.store;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Store Spring Data JPA Repository
 */
public interface StoreJpaRepository extends JpaRepository<StoreJpaEntity, Long> {
    
    /**
     * ID로 조회 (삭제되지 않은 가게만)
     */
    Optional<StoreJpaEntity> findByStoreIdAndDeletedAtIsNull(Long storeId);
    
    /**
     * 여러 ID로 조회 (삭제되지 않은 가게만)
     */
    List<StoreJpaEntity> findByStoreIdInAndDeletedAtIsNull(List<Long> storeIds);
    
    /**
     * Full-Text Search를 사용한 가게 검색 (관련성 점수 기반)
     * MySQL의 FULLTEXT INDEX와 ngram parser를 활용하여 한국어 검색 성능을 최적화합니다.
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 관련성 점수가 높은 순서로 정렬된 가게 목록
     */
    @Query(value = """
            SELECT s.* 
            FROM store s
            WHERE s.deleted_at IS NULL
              AND MATCH(s.name, s.description) AGAINST(:keyword IN NATURAL LANGUAGE MODE)
            ORDER BY MATCH(s.name, s.description) AGAINST(:keyword IN NATURAL LANGUAGE MODE) DESC,
                     s.view_count DESC,
                     s.name ASC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM store s
            WHERE s.deleted_at IS NULL
              AND MATCH(s.name, s.description) AGAINST(:keyword IN NATURAL LANGUAGE MODE)
            """,
            nativeQuery = true)
    Page<StoreJpaEntity> searchByFullText(
            @Param("keyword") String keyword,
            Pageable pageable
    );
    
    /**
     * 키워드로 가게명 또는 카테고리명 검색 (자동완성용)
     * Full-Text Search를 사용하여 검색 성능을 향상시킵니다.
     */
    @Query(value = """
            SELECT DISTINCT s.* 
            FROM store s
            LEFT JOIN category c ON s.category_id = c.category_id
            WHERE s.deleted_at IS NULL
              AND (MATCH(s.name, s.description) AGAINST(:keyword IN NATURAL LANGUAGE MODE)
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY MATCH(s.name, s.description) AGAINST(:keyword IN NATURAL LANGUAGE MODE) DESC,
                     s.view_count DESC,
                     s.name ASC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<StoreJpaEntity> searchByKeywordForAutocomplete(
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );
}

