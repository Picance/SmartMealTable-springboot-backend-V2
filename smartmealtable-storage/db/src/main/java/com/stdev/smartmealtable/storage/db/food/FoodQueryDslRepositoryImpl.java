package com.stdev.smartmealtable.storage.db.food;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodPageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.stdev.smartmealtable.storage.db.food.QFoodJpaEntity.foodJpaEntity;

/**
 * Food QueryDSL Repository 구현체
 * 복잡한 동적 쿼리를 처리합니다.
 */
@Repository
@RequiredArgsConstructor
public class FoodQueryDslRepositoryImpl implements FoodQueryDslRepository {
    
    private final JPAQueryFactory queryFactory;
    
    /**
     * 관리자용 음식 검색 (페이징, 삭제되지 않은 것만)
     */
    @Override
    public FoodPageResult adminSearch(Long categoryId, Long storeId, String name, int page, int size) {
        BooleanExpression condition = foodJpaEntity.deletedAt.isNull();

        if (categoryId != null) {
            condition = condition.and(foodJpaEntity.categoryId.eq(categoryId));
        }

        if (storeId != null) {
            condition = condition.and(foodJpaEntity.storeId.eq(storeId));
        }

        if (name != null && !name.isBlank()) {
            condition = condition.and(foodJpaEntity.foodName.containsIgnoreCase(name));
        }

        // 전체 개수 조회
        Long total = queryFactory
                .select(foodJpaEntity.count())
                .from(foodJpaEntity)
                .where(condition)
                .fetchOne();

        long totalElements = (total != null) ? total : 0;

        // 페이징 데이터 조회
        List<FoodJpaEntity> entities = queryFactory
                .selectFrom(foodJpaEntity)
                .where(condition)
                .orderBy(foodJpaEntity.registeredDt.desc()) // 최신 등록 순
                .offset((long) page * size)
                .limit(size)
                .fetch();

        List<Food> content = entities.stream()
                .map(FoodJpaEntity::toDomain)
                .collect(Collectors.toList());

        return FoodPageResult.of(content, page, size, totalElements);
    }

    /**
     * 카테고리가 음식에서 사용 중인지 확인 (삭제되지 않은 것만)
     */
    @Override
    public boolean existsByCategoryIdAndNotDeleted(Long categoryId) {
        Integer count = queryFactory
                .selectOne()
                .from(foodJpaEntity)
                .where(
                        foodJpaEntity.categoryId.eq(categoryId)
                                .and(foodJpaEntity.deletedAt.isNull())
                )
                .fetchFirst();
        return count != null;
    }

    /**
     * 가게가 음식에서 사용 중인지 확인 (삭제되지 않은 것만)
     */
    @Override
    public boolean existsByStoreIdAndNotDeleted(Long storeId) {
        Integer count = queryFactory
                .selectOne()
                .from(foodJpaEntity)
                .where(
                        foodJpaEntity.storeId.eq(storeId)
                                .and(foodJpaEntity.deletedAt.isNull())
                )
                .fetchFirst();
        return count != null;
    }
}
