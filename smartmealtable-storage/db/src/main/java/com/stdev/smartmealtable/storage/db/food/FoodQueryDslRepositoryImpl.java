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
    
    // ===== 자동완성 전용 메서드 (Phase 3) =====
    
    /**
     * 음식명 Prefix로 시작하는 음식 조회 (자동완성용)
     * 삭제되지 않은 음식만 조회하며, 대표 메뉴(isMain=true) 우선, 최신 등록순으로 정렬
     *
     * @param prefix 검색 접두사
     * @param limit 결과 제한 수
     * @return 음식 리스트
     */
    @Override
    public List<FoodJpaEntity> findByNameStartingWith(String prefix, int limit) {
        return queryFactory
                .selectFrom(foodJpaEntity)
                .where(
                        foodJpaEntity.foodName.startsWithIgnoreCase(prefix)
                                .and(foodJpaEntity.deletedAt.isNull())
                )
                .orderBy(
                        foodJpaEntity.isMain.desc().nullsLast(), // 대표 메뉴 우선
                        foodJpaEntity.registeredDt.desc() // 최신 등록순 (인기도 대리지표)
                )
                .limit(limit)
                .fetch();
    }
    
    /**
     * 여러 음식 ID로 조회 (캐시에서 가져온 ID로 조회)
     * 삭제되지 않은 음식만 조회
     * 
     * @param foodIds 음식 ID 리스트
     * @return 음식 리스트
     */
    @Override
    public List<FoodJpaEntity> findByFoodIdIn(List<Long> foodIds) {
        if (foodIds == null || foodIds.isEmpty()) {
            return List.of();
        }
        
        return queryFactory
                .selectFrom(foodJpaEntity)
                .where(
                        foodJpaEntity.foodId.in(foodIds)
                                .and(foodJpaEntity.deletedAt.isNull())
                )
                .fetch();
    }
}
