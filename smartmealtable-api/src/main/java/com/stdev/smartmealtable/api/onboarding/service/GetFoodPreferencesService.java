package com.stdev.smartmealtable.api.onboarding.service;

import com.stdev.smartmealtable.api.onboarding.service.dto.GetFoodPreferencesServiceResponse;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 온보딩 - 개별 음식 선호도 조회 Application Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetFoodPreferencesService {

    private final FoodPreferenceRepository foodPreferenceRepository;
    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 저장된 개별 음식 선호도 조회
     */
    public GetFoodPreferencesServiceResponse getFoodPreferences(Long memberId) {
        List<FoodPreference> preferences = foodPreferenceRepository.findByMemberIdAndIsPreferred(memberId, true);
        log.info("개별 음식 선호도 조회 - memberId: {}, preferenceCount: {}", memberId, preferences.size());

        if (preferences.isEmpty()) {
            return new GetFoodPreferencesServiceResponse(0, List.of(), List.of());
        }

        List<Long> preferredFoodIds = preferences.stream()
                .map(FoodPreference::getFoodId)
                .distinct()
                .toList();

        Map<Long, Food> foodMap = preferredFoodIds.isEmpty()
                ? Map.of()
                : foodRepository.findByIdIn(preferredFoodIds).stream()
                .collect(Collectors.toMap(Food::getFoodId, food -> food));

        Set<Long> categoryIds = foodMap.values().stream()
                .map(Food::getCategoryId)
                .collect(Collectors.toSet());

        Map<Long, String> categoryNameMap = categoryIds.isEmpty()
                ? Map.of()
                : categoryRepository.findByIdIn(new ArrayList<>(categoryIds)).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));

        Comparator<FoodPreference> preferredAtDesc = Comparator
                .comparing(FoodPreference::getPreferredAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed();

        List<GetFoodPreferencesServiceResponse.PreferredFoodInfo> preferredFoods = preferences.stream()
                .sorted(preferredAtDesc)
                .map(preference -> {
                    Food food = foodMap.get(preference.getFoodId());
                    String foodName = food != null ? food.getFoodName() : "알 수 없음";
                    String categoryName = (food != null)
                            ? categoryNameMap.getOrDefault(food.getCategoryId(), "알 수 없음")
                            : "알 수 없음";
                    String imageUrl = food != null ? food.getImageUrl() : null;

                    return new GetFoodPreferencesServiceResponse.PreferredFoodInfo(
                            preference.getFoodId(),
                            foodName,
                            categoryName,
                            imageUrl
                    );
                })
                .toList();

        return new GetFoodPreferencesServiceResponse(
                preferredFoodIds.size(),
                preferredFoodIds,
                preferredFoods
        );
    }
}
