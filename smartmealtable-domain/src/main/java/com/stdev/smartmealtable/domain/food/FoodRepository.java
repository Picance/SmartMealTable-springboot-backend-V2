package com.stdev.smartmealtable.domain.food;

import java.util.List;
import java.util.Optional;

/**
 * 음식 Repository 인터페이스
 */
public interface FoodRepository {

    /**
     * 음식 저장
     */
    Food save(Food food);

    /**
     * 음식 ID로 조회
     */
    Optional<Food> findById(Long foodId);

    /**
     * 여러 음식 ID로 조회
     */
    List<Food> findByIdIn(List<Long> foodIds);

    /**
     * 모든 음식 조회 (페이징은 Storage 계층에서 처리)
     */
    List<Food> findAll(int page, int size);

    /**
     * 카테고리별 음식 조회 (페이징은 Storage 계층에서 처리)
     */
    List<Food> findByCategoryId(Long categoryId, int page, int size);

    /**
     * 전체 음식 개수 조회
     */
    long count();

    /**
     * 카테고리별 음식 개수 조회
     */
    long countByCategoryId(Long categoryId);

    /**
     * 가게별 음식 조회
     */
    List<Food> findByStoreId(Long storeId);
}
