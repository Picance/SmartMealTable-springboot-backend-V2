package com.stdev.smartmealtable.storage.db.food;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Food Spring Data JPA Repository
 */
public interface FoodJpaRepository extends JpaRepository<FoodJpaEntity, Long> {

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
}
