package com.stdev.smartmealtable.storage.db.food;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Food Spring Data JPA Repository
 */
public interface FoodJpaRepository extends JpaRepository<FoodJpaEntity, Long>, FoodQueryDslRepository {

    /**
     * 음식 ID로 조회 (삭제되지 않은 것만)
     */
    Optional<FoodJpaEntity> findByFoodIdAndDeletedAtIsNull(Long foodId);

    /**
     * 카테고리별 음식 페이징 조회
     */
    Page<FoodJpaEntity> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * 여러 음식 ID로 조회
     */
    List<FoodJpaEntity> findByFoodIdIn(List<Long> foodIds);

    /**
     * 카테고리별 음식 개수 조회
     */
    long countByCategoryId(Long categoryId);

    /**
     * 가게별 음식 조회
     */
    List<FoodJpaEntity> findByStoreId(Long storeId);

    /**
     * 가게별 음식 삭제 (크롤러 배치용)
     */
    void deleteByStoreId(Long storeId);
}
