package com.stdev.smartmealtable.api.onboarding.service;

import com.stdev.smartmealtable.api.onboarding.service.dto.SaveFoodPreferencesServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.SaveFoodPreferencesServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodPreference;
import com.stdev.smartmealtable.domain.food.FoodPreferenceRepository;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 온보딩 - 개별 음식 선호도 저장 Application Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SaveFoodPreferencesService {

    private final FoodPreferenceRepository foodPreferenceRepository;
    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 개별 음식 선호도 저장
     */
    public SaveFoodPreferencesServiceResponse saveFoodPreferences(
            Long memberId,
            SaveFoodPreferencesServiceRequest request
    ) {
        List<Long> foodIds = deduplicateFoodIds(request.preferredFoodIds());
        log.info("개별 음식 선호도 저장 - memberId: {}, foodCount: {}", memberId, foodIds.size());

        // 1. 음식 존재 여부 검증
        List<Food> foods = foodIds.isEmpty()
                ? List.of()
                : foodRepository.findByIdIn(foodIds);
        if (!foodIds.isEmpty() && foods.size() != foodIds.size()) {
            throw new BusinessException(ErrorType.FOOD_NOT_FOUND);
        }

        // 2. 기존 선호도 삭제
        foodPreferenceRepository.deleteByMemberId(memberId);

        // 3. 새로운 선호도 저장
        List<FoodPreference> preferences = foodIds.stream()
                .map(foodId -> FoodPreference.create(memberId, foodId))
                .collect(Collectors.toList());

        preferences.forEach(foodPreferenceRepository::save);

        // 4. 응답 생성 (최대 10개만 반환)
        List<Food> responseFoods = foods.stream()
                .limit(10)
                .collect(Collectors.toList());

        // 5. 카테고리 정보 조회
        Map<Long, String> categoryNameMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));

        List<SaveFoodPreferencesServiceResponse.PreferredFoodInfo> preferredFoodInfos = responseFoods.stream()
                .map(food -> new SaveFoodPreferencesServiceResponse.PreferredFoodInfo(
                        food.getFoodId(),
                        food.getFoodName(),
                        categoryNameMap.get(food.getCategoryId()),
                        food.getImageUrl()
                ))
                .collect(Collectors.toList());

        return new SaveFoodPreferencesServiceResponse(
                foodIds.size(),
                preferredFoodInfos,
                "선호 음식이 성공적으로 저장되었습니다."
        );
    }

    private List<Long> deduplicateFoodIds(List<Long> preferredFoodIds) {
        if (preferredFoodIds == null || preferredFoodIds.isEmpty()) {
            return List.of();
        }
        return preferredFoodIds.stream()
                .distinct()
                .collect(Collectors.toList());
    }
}
