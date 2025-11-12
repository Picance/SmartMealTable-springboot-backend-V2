package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.api.home.service.dto.HomeDashboardServiceResponse;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
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
    private final MemberRepository memberRepository;
    private final DistanceCalculator distanceCalculator;
    private final MenuTagService menuTagService;
    private final LocationContextService locationContextService;
    private final BusinessHoursService businessHoursService;

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
            Double userLatitude,
            Double userLongitude,
            int limit
    ) {
        try {
            List<Food> foods;

            // 위치 정보가 있으면 거리 기반 필터링
            if (userLatitude != null && userLongitude != null) {
                foods = getMenusByDistance(userLatitude, userLongitude, limit * 2);
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
                            userLatitude,
                            userLongitude
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
            Double userLatitude,
            Double userLongitude,
            int limit
    ) {
        try {
            List<Store> stores;

            // 위치 정보가 있으면 거리 기반 필터링
            if (userLatitude != null && userLongitude != null) {
                stores = getStoresByDistance(userLatitude, userLongitude, limit * 2);
            } else {
                // 위치 정보 없이: 인기 가게 기반
                stores = getPopularStores(limit * 2);
            }

            // 추천 가게로 변환
            List<HomeDashboardServiceResponse.RecommendedStoreInfo> recommendations = stores.stream()
                    .limit(limit)
                    .map(store -> buildRecommendedStoreInfo(
                            store,
                            userLatitude,
                            userLongitude
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
            Double userLatitude,
            Double userLongitude
    ) {
        try {
            // 가게 정보 조회
            Store store = storeRepository.findById(food.getStoreId()).orElse(null);

            // 거리 계산
            BigDecimal distance = null;
            if (userLatitude != null && userLongitude != null && store != null) {
                Double storeLatitude = store.getLatitude() != null ? store.getLatitude().doubleValue() : null;
                Double storeLongitude = store.getLongitude() != null ? store.getLongitude().doubleValue() : null;

                if (storeLatitude != null && storeLongitude != null) {
                    distance = distanceCalculator.calculateDistanceKm(
                            userLatitude,
                            userLongitude,
                            storeLatitude,
                            storeLongitude
                    );
                }
            }

            String distanceText = distance != null ? distanceCalculator.formatDistance(distance) : null;

            // 태그 생성
            List<String> tags = menuTagService.generateMenuTags(
                    food.getFoodId(),
                    memberId,
                    dailyBudget
            );

            return new HomeDashboardServiceResponse.RecommendedMenuInfo(
                    food.getFoodId(),
                    food.getFoodName(),
                    store != null ? store.getName() : "알 수 없음",
                    food.getAveragePrice(),
                    distance,
                    distanceText,
                    tags
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
            Store store,
            Double userLatitude,
            Double userLongitude
    ) {
        try {
            // 거리 계산
            BigDecimal distance = null;
            if (userLatitude != null && userLongitude != null) {
                Double storeLatitude = store.getLatitude() != null ? store.getLatitude().doubleValue() : null;
                Double storeLongitude = store.getLongitude() != null ? store.getLongitude().doubleValue() : null;

                if (storeLatitude != null && storeLongitude != null) {
                    distance = distanceCalculator.calculateDistanceKm(
                            userLatitude,
                            userLongitude,
                            storeLatitude,
                            storeLongitude
                    );
                }
            }

            String distanceText = distance != null ? distanceCalculator.formatDistance(distance) : null;

            // 위치 맥락 정보 생성 (주소 정보 기반)
            String contextInfo = null;
            if (userLatitude != null && userLongitude != null && store.getAddress() != null) {
                // 주소에서 지역명 추출
                String addressParts[] = store.getAddress().split(" ");
                if (addressParts.length >= 3) {
                    String areaName = addressParts[2].replaceAll("(로|길|거리|街|街道)$", "");
                    if (distanceText != null) {
                        contextInfo = distanceText + " 떨어진 " + areaName + " 인근";
                    }
                }
            }

            // 영업 시간 정보 조회
            String businessHours = businessHoursService.getTodayBusinessHours(store.getStoreId());

            return new HomeDashboardServiceResponse.RecommendedStoreInfo(
                    store.getStoreId(),
                    store.getName(),
                    store.getCategoryIds() != null && !store.getCategoryIds().isEmpty() ? "카테고리" : "카테고리 미정",
                    store.getAveragePrice(),
                    distance,
                    distanceText,
                    businessHours,
                    contextInfo
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
    private List<Store> getStoresByDistance(Double latitude, Double longitude, int limit) {
        try {
            List<StoreWithDistance> storesWithDistance = storeRepository.findByDistanceOrderByDistance(
                    latitude,
                    longitude,
                    DEFAULT_SEARCH_RADIUS_KM.doubleValue(),
                    limit
            );

            log.debug("Fetched {} stores within {}km by distance", storesWithDistance.size(), DEFAULT_SEARCH_RADIUS_KM);
            return storesWithDistance.stream()
                    .map(StoreWithDistance::store)
                    .collect(Collectors.toList());

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
    private List<Store> getPopularStores(int limit) {
        try {
            List<Store> stores = storeRepository.findByPopularity(limit);
            log.debug("Fetched {} popular stores", stores.size());
            return stores;
        } catch (Exception e) {
            log.error("Error fetching popular stores", e);
            return List.of();
        }
    }
}
