package com.stdev.smartmealtable.storage.db.store;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StorePageResult;
import com.stdev.smartmealtable.domain.store.StoreRepository.StoreSearchResult;
import com.stdev.smartmealtable.domain.store.StoreType;
import com.stdev.smartmealtable.domain.store.StoreWithDistance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.stdev.smartmealtable.storage.db.category.QCategoryJpaEntity.categoryJpaEntity;
import static com.stdev.smartmealtable.storage.db.store.QStoreJpaEntity.storeJpaEntity;
import static com.stdev.smartmealtable.storage.db.store.QStoreCategoryJpaEntity.storeCategoryJpaEntity;

/**
 * Store QueryDSL Repository
 * 복잡한 동적 쿼리를 처리합니다.
 */
@Repository
@RequiredArgsConstructor
public class StoreQueryDslRepository {
    
    private final JPAQueryFactory queryFactory;
    
    /**
     * 조건에 맞는 가게 목록 조회
     * Haversine 공식을 사용하여 거리 계산
     */
    public StoreSearchResult searchStores(
            String keyword,
            BigDecimal userLatitude,
            BigDecimal userLongitude,
            Double radiusKm,
            Long categoryId,
            Boolean isOpenOnly,
            StoreType storeType,
            String sortBy,
            int page,
            int size
    ) {
        // Haversine 거리 계산 (km 단위)
        NumberExpression<Double> distanceExpression = calculateDistance(
                userLatitude.doubleValue(),
                userLongitude.doubleValue()
        );
        
        // 조건 생성
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(storeJpaEntity.deletedAt.isNull());
        
        if (keyword != null && !keyword.isBlank()) {
            conditions.add(
                    storeJpaEntity.name.containsIgnoreCase(keyword)
                            .or(categoryJpaEntity.name.containsIgnoreCase(keyword))
            );
        }
        
        if (radiusKm != null) {
            conditions.add(distanceExpression.loe(radiusKm));
        }
        
        if (categoryId != null) {
            conditions.add(storeCategoryJpaEntity.categoryId.eq(categoryId));
        }
        
        if (storeType != null) {
            conditions.add(storeJpaEntity.storeType.eq(storeType));
        }
        
        // TODO: isOpenOnly 구현은 영업시간 및 임시휴무 체크 로직 추가 필요
        // 현재는 기본 필터링만 수행
        
        // 전체 조건 결합
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElse(null);
        
        // Total count 조회
        Long totalCount = queryFactory
                .select(storeJpaEntity.count())
                .from(storeJpaEntity)
                .leftJoin(storeCategoryJpaEntity).on(storeJpaEntity.storeId.eq(storeCategoryJpaEntity.storeId))
                .where(finalCondition)
                .fetchOne();
        
        if (totalCount == null || totalCount == 0) {
            return new StoreSearchResult(List.of(), 0);
        }
        
        // 정렬 기준 결정
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(sortBy, distanceExpression);
        
        // 데이터 조회 (Store + Distance)
        List<Tuple> tuples = queryFactory
                .select(storeJpaEntity, distanceExpression)
                .from(storeJpaEntity)
                .leftJoin(storeCategoryJpaEntity).on(storeJpaEntity.storeId.eq(storeCategoryJpaEntity.storeId))
                .where(finalCondition)
                .orderBy(orderSpecifier)
                .offset((long) page * size)
                .limit(size)
                .fetch();
        
        List<StoreWithDistance> storesWithDistance = tuples.stream()
                .map(tuple -> {
                    StoreJpaEntity entity = tuple.get(storeJpaEntity);
                    Double distance = tuple.get(distanceExpression);
                    List<Long> categoryIds = queryCategoryIdsByStoreId(entity.getStoreId());
                    Store store = StoreEntityMapper.toDomain(entity, categoryIds);
                    return StoreWithDistance.of(store, distance);
                })
                .collect(Collectors.toList());
        
        return new StoreSearchResult(storesWithDistance, totalCount);
    }
    
    /**
     * Haversine 공식을 사용한 거리 계산 (단위: km)
     * 
     * distance = R * ACOS(COS(lat1) * COS(lat2) * COS(lon2 - lon1) + SIN(lat1) * SIN(lat2))
     * R = 6371 km (지구 반지름)
     */
    private NumberExpression<Double> calculateDistance(double userLat, double userLon) {
        // MySQL Haversine 공식을 사용한 거리 계산 (km 단위)
        // distance = R * ACOS(SIN(lat1) * SIN(lat2) + COS(lat1) * COS(lat2) * COS(lon2 - lon1))
        return Expressions.numberTemplate(Double.class,
                "6371.0 * ACOS(COS(RADIANS({0})) * COS(RADIANS({1})) * COS(RADIANS({2}) - RADIANS({3})) + SIN(RADIANS({0})) * SIN(RADIANS({1})))",
                userLat,
                storeJpaEntity.latitude,
                storeJpaEntity.longitude,
                userLon
        );
    }
    
    /**
     * 정렬 기준에 따른 OrderSpecifier 반환
     */
    private OrderSpecifier<?> getOrderSpecifier(String sortBy, NumberExpression<Double> distanceExpression) {
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "distance";
        }
        
        return switch (sortBy.toLowerCase()) {
            case "reviewcount" -> storeJpaEntity.reviewCount.desc();
            case "priceasc" -> storeJpaEntity.averagePrice.asc();
            case "pricedesc" -> storeJpaEntity.averagePrice.desc();
            case "favoritecount" -> storeJpaEntity.favoriteCount.desc();
            case "viewcount" -> storeJpaEntity.viewCount.desc();
            default -> distanceExpression.asc(); // distance (기본값)
        };
    }

    /**
     * 특정 가게의 카테고리 ID 목록 조회
     */
    private List<Long> queryCategoryIdsByStoreId(Long storeId) {
        return queryFactory
                .select(storeCategoryJpaEntity.categoryId)
                .from(storeCategoryJpaEntity)
                .where(storeCategoryJpaEntity.storeId.eq(storeId))
                .orderBy(storeCategoryJpaEntity.displayOrder.asc())
                .fetch();
    }

    // ===== ADMIN 전용 메서드 =====

    /**
     * 관리자용 음식점 검색 (페이징, 삭제되지 않은 것만)
     */
    public StorePageResult adminSearch(Long categoryId, String name, StoreType storeType, int page, int size) {
        BooleanExpression condition = storeJpaEntity.deletedAt.isNull();

        if (categoryId != null) {
            condition = condition.and(storeCategoryJpaEntity.categoryId.eq(categoryId));
        }

        if (name != null && !name.isBlank()) {
            condition = condition.and(storeJpaEntity.name.containsIgnoreCase(name));
        }

        if (storeType != null) {
            condition = condition.and(storeJpaEntity.storeType.eq(storeType));
        }

        // 전체 개수 조회
        Long total = queryFactory
                .select(storeJpaEntity.countDistinct())
                .from(storeJpaEntity)
                .leftJoin(storeCategoryJpaEntity).on(storeJpaEntity.storeId.eq(storeCategoryJpaEntity.storeId))
                .where(condition)
                .fetchOne();

        long totalElements = (total != null) ? total : 0;

        // 페이징 데이터 조회
        List<StoreJpaEntity> entities = queryFactory
                .selectDistinct(storeJpaEntity)
                .from(storeJpaEntity)
                .leftJoin(storeCategoryJpaEntity).on(storeJpaEntity.storeId.eq(storeCategoryJpaEntity.storeId))
                .where(condition)
                .orderBy(storeJpaEntity.registeredAt.desc()) // 최신 등록 순
                .offset((long) page * size)
                .limit(size)
                .fetch();

        List<Store> content = entities.stream()
                .map(entity -> {
                    List<Long> categoryIds = queryCategoryIdsByStoreId(entity.getStoreId());
                    return StoreEntityMapper.toDomain(entity, categoryIds);
                })
                .collect(Collectors.toList());

        return StorePageResult.of(content, page, size, totalElements);
    }

    /**
     * 카테고리가 음식점에서 사용 중인지 확인 (삭제되지 않은 것만)
     */
    public boolean existsByCategoryIdAndNotDeleted(Long categoryId) {
        Integer count = queryFactory
                .selectOne()
                .from(storeJpaEntity)
                .innerJoin(storeCategoryJpaEntity).on(storeJpaEntity.storeId.eq(storeCategoryJpaEntity.storeId))
                .where(
                        storeCategoryJpaEntity.categoryId.eq(categoryId)
                                .and(storeJpaEntity.deletedAt.isNull())
                )
                .fetchFirst();
        return count != null;
    }
}
