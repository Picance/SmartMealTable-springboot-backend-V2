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
import com.stdev.smartmealtable.storage.db.search.SearchKeywordSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.stdev.smartmealtable.storage.db.store.QStoreJpaEntity.storeJpaEntity;
import static com.stdev.smartmealtable.storage.db.store.QStoreCategoryJpaEntity.storeCategoryJpaEntity;
import static com.stdev.smartmealtable.storage.db.store.QStoreSearchKeywordJpaEntity.storeSearchKeywordJpaEntity;

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
            // N:N 관계 변경으로 인해 가게명만 검색
            // 카테고리명 검색은 별도로 필요시 구현
            conditions.add(storeJpaEntity.name.containsIgnoreCase(keyword));
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
        
        // Total count 조회 (distinct로 유니크한 store만 카운팅)
        Long totalCount = queryFactory
                .select(storeJpaEntity.storeId.countDistinct())
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
    
    // ===== 자동완성 전용 메서드 (Phase 3) =====

    /**
     * 가게명 또는 카테고리명을 기준으로 검색 (자동완성용)
     *
     * 검색 전략 (우선순위):
     * 1. 가게명이 keyword로 시작하는 경우 (prefix match - 최고 점수)
     * 2. 가게명에 keyword가 포함되는 경우 (substring match)
     * 3. 카테고리명에 keyword가 포함되는 경우 (category match)
     *
     * 삭제되지 않은 가게만 조회하며, popularity(favoriteCount) 높은 순으로 정렬
     *
     * @param keyword 검색 키워드
     * @param limit 결과 제한 수
     * @return 가게 리스트
     */
    public List<StoreJpaEntity> findByNameStartingWith(String keyword, int limit) {
        String normalized = normalizeForSearch(keyword);
        if (normalized.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .selectFrom(storeJpaEntity)
            .where(
                storeJpaEntity.nameNormalized.startsWith(normalized)
                    .and(storeJpaEntity.deletedAt.isNull())
            )
            .orderBy(
                // popularity 기준 정렬
                storeJpaEntity.favoriteCount.desc().nullsLast()
            )
            .limit(limit)
            .fetch();
    }
    
    /**
     * 가게명에 keyword가 포함되는 가게 조회 (자동완성용)
     * 가게 이름 중간에 있는 키워드 검색용
     *
     * @param keyword 검색 키워드
     * @param limit 결과 제한 수
     * @return 가게 리스트 (popularity 높은 순으로 정렬)
     */
    public List<StoreJpaEntity> findByNameContaining(String keyword, int limit) {
        String normalized = normalizeForSearch(keyword);
        if (normalized.isEmpty()) {
            return List.of();
        }

        String prefix = SearchKeywordSupport.buildQueryPrefix(normalized);
        if (prefix.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectDistinct(storeJpaEntity)
                .from(storeJpaEntity)
                .innerJoin(storeSearchKeywordJpaEntity)
                .on(storeJpaEntity.storeId.eq(storeSearchKeywordJpaEntity.storeId))
                .where(
                        storeJpaEntity.deletedAt.isNull()
                                .and(storeSearchKeywordJpaEntity.keywordType.eq(StoreSearchKeywordType.NAME_SUBSTRING))
                                .and(storeSearchKeywordJpaEntity.keywordPrefix.like(prefix + "%"))
                                .and(storeSearchKeywordJpaEntity.keyword.like(normalized + "%"))
                )
                .orderBy(storeJpaEntity.favoriteCount.desc().nullsLast())
                .limit(limit)
                .fetch();
    }

    private String normalizeForSearch(String keyword) {
        return SearchKeywordSupport.normalize(keyword);
    }

    /**
     * 여러 가게 ID로 조회 (캐시에서 가져온 ID로 조회)
     * 삭제되지 않은 가게만 조회
     *
     * @param storeIds 가게 ID 리스트
     * @return 가게 리스트
     */
    public List<StoreJpaEntity> findByStoreIdIn(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(storeJpaEntity)
                .where(
                        storeJpaEntity.storeId.in(storeIds)
                                .and(storeJpaEntity.deletedAt.isNull())
                )
                .fetch();
    }

    /**
     * 위치 기반 가게 조회 (거리순 정렬)
     * 사용자 위치에서 일정 반경 내의 가게들을 거리순으로 반환합니다.
     *
     * @param userLatitude 사용자 위도
     * @param userLongitude 사용자 경도
     * @param radiusKm 검색 반경 (km)
     * @param limit 조회 개수 제한
     * @return 거리 정보를 포함한 가게 목록
     */
    public List<StoreWithDistance> findByDistanceOrderByDistance(
            double userLatitude,
            double userLongitude,
            double radiusKm,
            int limit
    ) {
        // Haversine 거리 계산 (km 단위)
        NumberExpression<Double> distanceExpression = calculateDistance(userLatitude, userLongitude);

        List<Tuple> tuples = queryFactory
                .select(storeJpaEntity, distanceExpression)
                .from(storeJpaEntity)
                .where(
                        distanceExpression.loe(radiusKm)
                                .and(storeJpaEntity.deletedAt.isNull())
                )
                .orderBy(distanceExpression.asc())
                .limit(limit)
                .fetch();

        return tuples.stream()
                .map(tuple -> {
                    StoreJpaEntity entity = tuple.get(storeJpaEntity);
                    Double distance = tuple.get(distanceExpression);
                    List<Long> categoryIds = queryCategoryIdsByStoreId(entity.getStoreId());
                    Store store = StoreEntityMapper.toDomain(entity, categoryIds);
                    return StoreWithDistance.of(store, distance);
                })
                .collect(Collectors.toList());
    }

    /**
     * 인기순 가게 조회 (좋아요 개수 기준)
     * 삭제되지 않은 가게 중 인기도가 높은 순서로 반환합니다.
     *
     * @param limit 조회 개수 제한
     * @return 가게 리스트 (인기순 정렬)
     */
    public List<StoreJpaEntity> findByPopularity(int limit) {
        return queryFactory
                .selectFrom(storeJpaEntity)
                .where(storeJpaEntity.deletedAt.isNull())
                .orderBy(
                        storeJpaEntity.favoriteCount.desc().nullsLast(),
                        storeJpaEntity.reviewCount.desc().nullsLast(),
                        storeJpaEntity.viewCount.desc().nullsLast()
                )
                .limit(limit)
                .fetch();
    }
}
