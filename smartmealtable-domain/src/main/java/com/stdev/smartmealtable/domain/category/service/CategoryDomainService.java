package com.stdev.smartmealtable.domain.category.service;

import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 도메인 서비스
 * 
 * <p>비즈니스 로직을 담당하며, Repository를 통해 데이터에 접근합니다.</p>
 * <p>도메인 모듈은 POJO를 유지하기 위해 Spring Data 의존성을 사용하지 않습니다.</p>
 */
@RequiredArgsConstructor
public class CategoryDomainService {

    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 저장
     */
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * 카테고리 ID로 조회
     */
    public Optional<Category> findById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    /**
     * 모든 카테고리 조회
     */
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    /**
     * 여러 카테고리 ID로 조회
     */
    public List<Category> findByIdIn(List<Long> categoryIds) {
        return categoryRepository.findByIdIn(categoryIds);
    }
}
