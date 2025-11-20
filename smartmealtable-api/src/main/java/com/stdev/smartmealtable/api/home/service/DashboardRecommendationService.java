package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.api.home.service.dto.HomeDashboardServiceResponse;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreWithDistance;
import com.stdev.smartmealtable.support.location.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 대시보드 추천 엔진 서비스
 *
 * 홈 대시보드에 표시할 추천 메뉴와 가게를 생성합니다.
 * 사용자의 위치, 예산, 선호도를 종합적으로 고려합니다.
 *
 * @author SmartMealTable Team
 * @since 2025-11-12
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DashboardRecommendationService {

    private final FoodRepository foodRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final DistanceCalculator distanceCalculator;
    private final MenuTagService menuTagService;
    private final LocationContextService locationContextService;

    private static final int DEFAULT_RECOMMENDATION_LIMIT = 5; // 기본 추천 개수
    private static final BigDecimal DEFAULT_SEARCH_RADIUS_KM = new BigDecimal("3"); // 기본 검색 반경 3km

    /**
     * 대시보드용 추천 메뉴 생성
     *
     * @param memberId 사용자 ID
     * @param dailyBudget 일일 예산
     * @param userLatitude 사용자 위도 (nullable)
     * @param userLongitude 사용자 경도 (nullable)
     * @param limit 추천 개수 제한
     * @return 추천 메뉴 목록
     */
    public List<HomeDashboardServiceResponse.RecommendedMenuInfo> getRecommendedMenus(
            Long memberId,
            BigDecimal dailyBudget,
            Double referenceLatitude,
            Double referenceLongitude,
            int limit
    ) {
        try {
            List<Food> foods;

            // 위치 정보가 있으면 거리 기반 필터링
            if (referenceLatitude != null && referenceLongitude != null) {
                foods = getMenusByDistance(referenceLatitude, referenceLongitude, limit * 2);
            } else {
                // 위치 정보 없이: 인기 메뉴 기반
                foods = getPopularMenus(limit * 2);
            }

            // 추천 메뉴로 변환
            List<HomeDashboardServiceResponse.RecommendedMenuInfo> recommendations = foods.stream()
                    .limit(limit)
                    .map(food -> buildRecommendedMenuInfo(
                            food,
                            memberId,
                            dailyBudget,
                            referenceLatitude,
                            referenceLongitude
                    ))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.debug("Generated {} recommended menus for member {}", recommendations.size(), memberId);
            return recommendations;

        } catch (Exception e) {
            log.error("Error generating recommended menus", e);
            return List.of();
        }
    }

    /**
     * 대시보드용 추천 가게 생성
     *
     * @param memberId 사용자 ID
     * @param userLatitude 사용자 위도 (nullable)
     * @param userLongitude 사용자 경도 (nullable)
     * @param limit 추천 개수 제한
     * @return 추천 가게 목록
     */
    public List<HomeDashboardServiceResponse.RecommendedStoreInfo> getRecommendedStores(
            Long memberId,
            Double referenceLatitude,
            Double referenceLongitude,
            int limit
    ) {
        try {
            List<StoreWithDistance> stores;

            // 위치 정보가 있으면 거리 기반 필터링
            if (referenceLatitude != null && referenceLongitude != null) {
                stores = getStoresByDistance(referenceLatitude, referenceLongitude, limit * 2);
            } else {
                // 위치 정보 없이: 인기 가게 기반
                stores = getPopularStores(limit * 2);
            }

            // 추천 가게로 변환
            List<HomeDashboardServiceResponse.RecommendedStoreInfo> recommendations = stores.stream()
                    .limit(limit)
                    .map(store -> buildRecommendedStoreInfo(
                            store,
                            referenceLatitude,
                            referenceLongitude
                    ))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.debug("Generated {} recommended stores for member {}", recommendations.size(), memberId);
            return recommendations;

        } catch (Exception e) {
            log.error("Error generating recommended stores", e);
            return List.of();
        }
    }

    /**
     * 추천 메뉴 정보 구성
     */
    private HomeDashboardServiceResponse.RecommendedMenuInfo buildRecommendedMenuInfo(
            Food food,
            Long memberId,
            BigDecimal dailyBudget,
            Double referenceLatitude,
            Double referenceLongitude
    ) {
        try {
            Store store = storeRepository.findById(food.getStoreId()).orElse(null);

            BigDecimal distance = calculateDistance(store, referenceLatitude, referenceLongitude);
            String distanceText = formatDistanceText(distance);

            List<String> tags = menuTagService.generateMenuTags(
                    food.getFoodId(),
                    memberId,
                    dailyBudget
            );
            String primaryTag = tags.isEmpty() ? null : tags.get(0);

            Integer price = food.getPrice() != null ? food.getPrice() : food.getAveragePrice();
            if (price == null) {
                price = 0;
            }

            return new HomeDashboardServiceResponse.RecommendedMenuInfo(
                    food.getFoodId(),
                    food.getFoodName(),
                    price,
                    food.getStoreId(),
                    store != null ? store.getName() : "알 수 없음",
                    distance,
                    distanceText,
                    tags,
                    primaryTag,
                    food.getImageUrl()
            );

        } catch (Exception e) {
            log.debug("Error building recommended menu info for food {}", food.getFoodId(), e);
            return null;
        }
    }

    /**
     * 추천 가게 정보 구성
     */
    private HomeDashboardServiceResponse.RecommendedStoreInfo buildRecommendedStoreInfo(
            StoreWithDistance storeWithDistance,
            Double referenceLatitude,
            Double referenceLongitude
    ) {
        Store store = storeWithDistance.store();
        try {
            BigDecimal distance = storeWithDistance.distance();
            if (distance == null) {
                distance = calculateDistance(store, referenceLatitude, referenceLongitude);
            }
            String distanceText = formatDistanceText(distance);

            String contextInfo = locationContextService.generateLocationContext(
                    store,
                    referenceLatitude,
                    referenceLongitude
            );

            String categoryName = getPrimaryCategoryName(store);
            Integer averagePrice = store.getAveragePrice();
            Integer reviewCount = store.getReviewCount();
            String imageUrl = store.getImageUrl();
            String tag = buildStoreTag(store, categoryName, averagePrice, reviewCount);

            return new HomeDashboardServiceResponse.RecommendedStoreInfo(
                    store.getStoreId(),
                    store.getName(),
                    categoryName,
                    distance,
                    distanceText,
                    contextInfo,
                    averagePrice,
                    reviewCount,
                    imageUrl,
                    tag
            );

        } catch (Exception e) {
            log.debug("Error building recommended store info for store {}", store.getStoreId(), e);
            return null;
        }
    }

    /**
     * 거리별 메뉴 조회
     *
     * QueryDSL을 이용한 지리적 쿼리로 구현:
     * 1. 사용자 위치 기준으로 거리 내 가게들을 검색
     * 2. 각 가게별로 인기 메뉴(대표 메뉴 우선, 최신 등록순) 조회
     * 3. 거리순 정렬된 메뉴 목록 반환
     */
    private List<Food> getMenusByDistance(Double latitude, Double longitude, int limit) {
        try {
            BigDecimal userLatitude = BigDecimal.valueOf(latitude);
            BigDecimal userLongitude = BigDecimal.valueOf(longitude);

            // 기본 검색 반경 3km 이내의 가게들을 거리순으로 조회
            List<StoreWithDistance> storesWithDistance = storeRepository.findByDistanceOrderByDistance(
                    latitude,
                    longitude,
                    DEFAULT_SEARCH_RADIUS_KM.doubleValue(),
                    limit
            );

            // 조회된 가게들에서 인기 메뉴를 모아서 반환
            List<Food> menus = new ArrayList<>();
            for (StoreWithDistance storeWithDistance : storesWithDistance) {
                Long storeId = storeWithDistance.store().getStoreId();
                List<Food> storeMenus = foodRepository.findByStoreIdOrderByDistance(
                        storeId,
                        latitude,
                        longitude,
                        3  // 가게당 최대 3개 메뉴
                );
                menus.addAll(storeMenus);

                if (menus.size() >= limit) {
                    break;
                }
            }

            log.debug("Fetched {} menus within {}km by distance", menus.size(), DEFAULT_SEARCH_RADIUS_KM);
            return menus.stream().limit(limit).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching menus by distance", e);
            return List.of();
        }
    }

    /**
     * 인기 메뉴 조회
     *
     * 전국 인기 메뉴를 대표 메뉴 우선, 최신 등록순으로 반환합니다.
     * 조회는 Repository의 인기도 기반 쿼리를 활용합니다.
     */
    private List<Food> getPopularMenus(int limit) {
        try {
            List<Food> menus = foodRepository.findByPopularity(limit);
            log.debug("Fetched {} popular menus", menus.size());
            return menus;
        } catch (Exception e) {
            log.error("Error fetching popular menus", e);
            return List.of();
        }
    }

    /**
     * 거리별 가게 조회
     *
     * QueryDSL을 이용한 지리적 쿼리로 구현:
     * Haversine 공식을 사용하여 사용자 위치에서 일정 반경 내의 가게들을
     * 거리순으로 정렬하여 반환합니다.
     */
    private List<StoreWithDistance> getStoresByDistance(Double latitude, Double longitude, int limit) {
        try {
            List<StoreWithDistance> storesWithDistance = storeRepository.findByDistanceOrderByDistance(
                    latitude,
                    longitude,
                    DEFAULT_SEARCH_RADIUS_KM.doubleValue(),
                    limit
            );

            log.debug("Fetched {} stores within {}km by distance", storesWithDistance.size(), DEFAULT_SEARCH_RADIUS_KM);
            return storesWithDistance;

        } catch (Exception e) {
            log.error("Error fetching stores by distance", e);
            return List.of();
        }
    }

    /**
     * 인기 가게 조회
     *
     * 전국 인기 가게를 좋아요 > 리뷰 수 > 조회 수 순으로 반환합니다.
     * 조회는 Repository의 인기도 기반 쿼리를 활용합니다.
     */
    private List<StoreWithDistance> getPopularStores(int limit) {
        try {
            List<Store> stores = storeRepository.findByPopularity(limit);
            log.debug("Fetched {} popular stores", stores.size());
            return stores.stream()
                    .map(store -> StoreWithDistance.of(store, (BigDecimal) null))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching popular stores", e);
            return List.of();
        }
    }

    private String formatDistanceText(BigDecimal distance) {
        if (distance == null) {
            return null;
        }
        try {
            return distanceCalculator.formatDistance(distance);
        } catch (Exception e) {
            log.debug("Error formatting distance text", e);
            return null;
        }
    }

    private BigDecimal calculateDistance(Store store, Double referenceLatitude, Double referenceLongitude) {
        if (store == null || referenceLatitude == null || referenceLongitude == null) {
            return null;
        }

        Double storeLatitude = store.getLatitude() != null ? store.getLatitude().doubleValue() : null;
        Double storeLongitude = store.getLongitude() != null ? store.getLongitude().doubleValue() : null;

        if (storeLatitude == null || storeLongitude == null) {
            return null;
        }

        try {
            return distanceCalculator.calculateDistanceKm(
                    referenceLatitude,
                    referenceLongitude,
                    storeLatitude,
                    storeLongitude
            );
        } catch (Exception e) {
            log.debug("Error calculating distance for store {}", store.getStoreId(), e);
            return null;
        }
    }

    private String getPrimaryCategoryName(Store store) {
        if (store.getCategoryIds() == null || store.getCategoryIds().isEmpty()) {
            return "카테고리 미정";
        }

        Long primaryCategoryId = store.getCategoryIds().get(0);
        return categoryRepository.findById(primaryCategoryId)
                .map(Category::getName)
                .orElse("카테고리 미정");
    }

    private String buildStoreTag(
            Store store,
            String categoryName,
            Integer averagePrice,
            Integer reviewCount
    ) {
        if (store == null) {
            return null;
        }

        if (store.isCampusRestaurant()) {
            return "학식";
        }

        if (averagePrice != null && averagePrice <= 7000) {
            return "가성비";
        }

        if (reviewCount != null && reviewCount >= 500) {
            return "인기";
        }

        return categoryName;
    }
}
