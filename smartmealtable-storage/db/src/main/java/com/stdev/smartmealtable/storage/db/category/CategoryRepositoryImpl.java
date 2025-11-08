package com.stdev.smartmealtable.storage.db.category;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryPageResult;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.storage.db.food.QFoodJpaEntity;
import com.stdev.smartmealtable.storage.db.store.QStoreJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.stdev.smartmealtable.storage.db.category.QCategoryJpaEntity.categoryJpaEntity;

/**
 * 카테고리 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;
    private final JPAQueryFactory queryFactory;

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

    @Override
    public CategoryPageResult searchByName(String name, int page, int size) {
        BooleanExpression condition = nameContains(name);

        // 전체 개수 조회
        Long total = queryFactory
                .select(categoryJpaEntity.count())
                .from(categoryJpaEntity)
                .where(condition)
                .fetchOne();

        long totalElements = (total != null) ? total : 0;

        // 페이징 데이터 조회
        List<CategoryJpaEntity> entities = queryFactory
                .selectFrom(categoryJpaEntity)
                .where(condition)
                .orderBy(categoryJpaEntity.name.asc())
                .offset((long) page * size)
                .limit(size)
                .fetch();

        List<Category> content = entities.stream()
                .map(CategoryJpaEntity::toDomain)
                .collect(Collectors.toList());

        return CategoryPageResult.of(content, page, size, totalElements);
    }

    @Override
    public boolean existsByName(String name) {
        Integer count = queryFactory
                .selectOne()
                .from(categoryJpaEntity)
                .where(categoryJpaEntity.name.eq(name))
                .fetchFirst();
        return count != null;
    }

    @Override
    public Optional<Category> findByName(String name) {
        return categoryJpaRepository.findByName(name)
            .map(CategoryJpaEntity::toDomain);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long categoryId) {
        Integer count = queryFactory
                .selectOne()
                .from(categoryJpaEntity)
                .where(
                        categoryJpaEntity.name.eq(name),
                        categoryJpaEntity.categoryId.ne(categoryId)
                )
                .fetchFirst();
        return count != null;
    }

    @Override
    public void deleteById(Long categoryId) {
        categoryJpaRepository.deleteById(categoryId);
    }

    @Override
    public boolean isUsedInStoreOrFood(Long categoryId) {
        QStoreJpaEntity store = QStoreJpaEntity.storeJpaEntity;
        QFoodJpaEntity food = QFoodJpaEntity.foodJpaEntity;

        // Store에서 사용 중인지 확인
        Integer storeCount = queryFactory
                .selectOne()
                .from(store)
                .where(
                        store.categoryId.eq(categoryId),
                        store.deletedAt.isNull()  // 삭제되지 않은 음식점만
                )
                .fetchFirst();

        if (storeCount != null) {
            return true;
        }

        // Food에서 사용 중인지 확인
        Integer foodCount = queryFactory
                .selectOne()
                .from(food)
                .where(food.categoryId.eq(categoryId))
                .fetchFirst();

        return foodCount != null;
    }

    /**
     * 이름 검색 조건
     */
    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ?
                categoryJpaEntity.name.containsIgnoreCase(name) : null;
    }
}
