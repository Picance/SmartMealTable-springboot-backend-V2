package com.stdev.smartmealtable.storage.db.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * StoreCategoryJpaEntity에 대한 Spring Data JPA Repository
 */
public interface StoreCategoryJpaRepository extends JpaRepository<StoreCategoryJpaEntity, Long> {
    
    /**
     * 특정 가게에 속한 모든 카테고리 ID 조회
     */
    @Query("SELECT sc.categoryId FROM StoreCategoryJpaEntity sc WHERE sc.storeId = :storeId ORDER BY sc.displayOrder ASC")
    List<Long> findCategoryIdsByStoreId(@Param("storeId") Long storeId);
    
    /**
     * 특정 가게에 속한 모든 매핑 레코드 조회
     */
    List<StoreCategoryJpaEntity> findByStoreId(Long storeId);
    
    /**
     * 특정 카테고리에 속한 모든 가게 ID 조회
     */
    @Query("SELECT sc.storeId FROM StoreCategoryJpaEntity sc WHERE sc.categoryId = :categoryId ORDER BY sc.displayOrder ASC")
    List<Long> findStoreIdsByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * 가게 삭제 시 해당 가게의 모든 카테고리 매핑 제거
     */
    @Modifying
    @Query("DELETE FROM StoreCategoryJpaEntity sc WHERE sc.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
    
    /**
     * 카테고리 삭제 시 해당 카테고리의 모든 가게 매핑 제거
     */
    @Modifying
    @Query("DELETE FROM StoreCategoryJpaEntity sc WHERE sc.categoryId = :categoryId")
    void deleteByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * 특정 가게-카테고리 매핑 여부 확인
     */
    boolean existsByStoreIdAndCategoryId(Long storeId, Long categoryId);
}
