package com.stdev.smartmealtable.storage.db.category;

import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 카테고리 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Category save(Category category) {
        CategoryJpaEntity entity = CategoryJpaEntity.fromDomain(category);
        CategoryJpaEntity savedEntity = categoryJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Category> findById(Long categoryId) {
        return categoryJpaRepository.findById(categoryId)
            .map(CategoryJpaEntity::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll().stream()
            .map(CategoryJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByIdIn(List<Long> categoryIds) {
        return categoryJpaRepository.findAllById(categoryIds).stream()
            .map(CategoryJpaEntity::toDomain)
            .collect(Collectors.toList());
    }
}
