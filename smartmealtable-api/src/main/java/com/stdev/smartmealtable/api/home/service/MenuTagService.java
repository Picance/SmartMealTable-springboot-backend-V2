package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.food.FoodPreference;
import com.stdev.smartmealtable.domain.food.FoodPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 메뉴 태그 생성 서비스
 *
 * 메뉴에 대한 다양한 태그를 생성합니다:
 * - 인기 메뉴: 인기도 스코어 기반
 * - 예산 적합: 사용자 일일 예산 기반
 * - 추천: 사용자의 음식 선호도 기반
 * - 신제품: 최근 추가된 메뉴
 *
 * @author SmartMealTable Team
 * @since 2025-11-12
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MenuTagService {

    private final FoodRepository foodRepository;
    private final FoodPreferenceRepository foodPreferenceRepository;

    private static final double POPULAR_FOOD_THRESHOLD = 4.0; // 인기도 4.0 이상
    private static final double BUDGET_RATIO = 0.8; // 예산의 80% 이하

    /**
     * 메뉴에 대한 태그 생성
     *
     * @param foodId 음식 ID
     * @param memberId 사용자 ID
     * @param dailyBudget 일일 예산
     * @return 태그 목록 (중복 없음)
     */
    public List<String> generateMenuTags(
            Long foodId,
            Long memberId,
            BigDecimal dailyBudget
    ) {
        Set<String> tags = new HashSet<>();

        try {
            Food food = foodRepository.findById(foodId).orElse(null);
            if (food == null) {
                log.warn("Food not found: foodId={}", foodId);
                return List.of();
            }

            // 1. 인기 메뉴 태그
            if (isPopularFood(food)) {
                tags.add("인기메뉴");
            }

            // 2. 예산 적합 태그
            if (isBudgetFriendly(food, dailyBudget)) {
                tags.add("예산적합");
            }

            // 3. 추천 태그 (사용자 선호도)
            if (isUserPreferredFood(foodId, memberId)) {
                tags.add("추천");
            }

            // 4. 신제품 태그
            if (isNewProduct(food)) {
                tags.add("신제품");
            }

            log.debug("Generated tags for food {}: {}", foodId, tags);

        } catch (Exception e) {
            log.error("Error generating tags for food {}", foodId, e);
        }

        return new ArrayList<>(tags);
    }

    /**
     * 여러 메뉴에 대한 태그 일괄 생성
     *
     * @param foodIds 음식 ID 목록
     * @param memberId 사용자 ID
     * @param dailyBudget 일일 예산
     * @return 음식 ID → 태그 목록 맵
     */
    public java.util.Map<Long, List<String>> generateMenuTagsBatch(
            List<Long> foodIds,
            Long memberId,
            BigDecimal dailyBudget
    ) {
        java.util.Map<Long, List<String>> result = new java.util.HashMap<>();

        for (Long foodId : foodIds) {
            result.put(foodId, generateMenuTags(foodId, memberId, dailyBudget));
        }

        return result;
    }

    /**
     * 인기 메뉴 판정
     *
     * TODO: 향후 Food 엔티티에 popularityScore 필드 추가 후 구현
     * 현재는 리뷰 수를 기반으로 인기도 판단
     *
     * @param food 음식 정보
     * @return 인기 메뉴 여부
     */
    private boolean isPopularFood(Food food) {
        // 향후 popularityScore 필드 사용 가능 시 아래로 변경
        // Double popularityScore = food.getPopularityScore();
        // return popularityScore != null && popularityScore >= POPULAR_FOOD_THRESHOLD;

        // 현재는 false 반환 (popularityScore 미지원)
        return false;
    }

    /**
     * 예산 친화적 메뉴 판정
     *
     * @param food 음식 정보
     * @param dailyBudget 일일 예산
     * @return 예산 친화적 여부
     */
    private boolean isBudgetFriendly(Food food, BigDecimal dailyBudget) {
        if (dailyBudget == null || dailyBudget.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        Integer price = food.getAveragePrice();
        if (price == null) {
            return false;
        }

        BigDecimal budgetThreshold = dailyBudget.multiply(BigDecimal.valueOf(BUDGET_RATIO));
        return BigDecimal.valueOf(price).compareTo(budgetThreshold) <= 0;
    }

    /**
     * 사용자 선호 메뉴 판정
     *
     * @param foodId 음식 ID
     * @param memberId 사용자 ID
     * @return 사용자 선호 메뉴 여부
     */
    private boolean isUserPreferredFood(Long foodId, Long memberId) {
        try {
            // 사용자의 음식 선호도에서 현재 음식이 있는지 확인
            List<FoodPreference> preferences = foodPreferenceRepository.findByMemberId(memberId);

            for (FoodPreference preference : preferences) {
                if (preference.getFoodId().equals(foodId)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Error checking user preference for food {}", foodId, e);
        }

        return false;
    }

    /**
     * 신제품 판정 (최근 30일 내에 추가된 메뉴)
     *
     * @param food 음식 정보
     * @return 신제품 여부
     */
    private boolean isNewProduct(Food food) {
        if (food.getRegisteredDt() == null) {
            return false;
        }

        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now()
                .minusDays(30);

        return food.getRegisteredDt().isAfter(thirtyDaysAgo);
    }
}
