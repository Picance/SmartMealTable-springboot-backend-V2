package com.stdev.smartmealtable.domain.category;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 Repository 인터페이스
 */
public interface CategoryRepository {

    /**
     * 카테고리 ID로 조회
     */
    Optional<Category> findById(Long categoryId);

    /**
     * 모든 카테고리 조회
     */
    List<Category> findAll();
}
