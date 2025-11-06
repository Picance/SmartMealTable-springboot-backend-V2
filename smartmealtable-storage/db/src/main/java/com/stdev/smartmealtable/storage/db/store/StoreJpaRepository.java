package com.stdev.smartmealtable.storage.db.store;

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
     * 외부 크롤링 ID로 조회
     * 배치 작업에서 기존 가게 찾기에 사용
     */
    Optional<StoreJpaEntity> findByExternalId(String externalId);
    
    /**
     * 여러 ID로 조회 (삭제되지 않은 가게만)
     */
    List<StoreJpaEntity> findByStoreIdInAndDeletedAtIsNull(List<Long> storeIds);
    
    /**
     * 키워드로 가게명 또는 카테고리명 검색 (자동완성용)
     * 가게명에 키워드가 포함되어 있거나, 가게의 카테고리명에 키워드가 포함된 가게를 검색합니다.
     */
    @Query("""
            SELECT s FROM StoreJpaEntity s
            LEFT JOIN CategoryJpaEntity c ON s.categoryId = c.categoryId
            WHERE s.deletedAt IS NULL
              AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY s.viewCount DESC, s.name ASC
            LIMIT :limit
            """)
    List<StoreJpaEntity> searchByKeywordForAutocomplete(
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );
}
