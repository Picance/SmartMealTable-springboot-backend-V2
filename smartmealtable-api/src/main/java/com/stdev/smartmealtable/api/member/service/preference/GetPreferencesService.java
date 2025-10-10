package com.stdev.smartmealtable.api.member.service.preference;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodPreference;
import com.stdev.smartmealtable.domain.food.FoodPreferenceRepository;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 선호도 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPreferencesService {

    private final MemberRepository memberRepository;
    private final PreferenceRepository preferenceRepository;
    private final FoodPreferenceRepository foodPreferenceRepository;
    private final CategoryRepository categoryRepository;
    private final FoodRepository foodRepository;

    public GetPreferencesServiceResponse execute(Long memberId) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 카테고리 선호도 조회
        List<Preference> preferences = preferenceRepository.findByMemberId(memberId);
        Map<Long, String> categoryMap = getCategoryNameMap(preferences);

        List<GetPreferencesServiceResponse.CategoryPreferenceDto> categoryPreferences = preferences.stream()
                .map(pref -> GetPreferencesServiceResponse.CategoryPreferenceDto.builder()
                        .preferenceId(pref.getPreferenceId())
                        .categoryId(pref.getCategoryId())
                        .categoryName(categoryMap.getOrDefault(pref.getCategoryId(), "알 수 없음"))
                        .weight(pref.getWeight())
                        .build())
                .collect(Collectors.toList());

        // 음식 선호도 조회
        List<FoodPreference> foodPreferences = foodPreferenceRepository.findByMemberId(memberId);
        Map<Long, Food> foodMap = getFoodMap(foodPreferences);
        Map<Long, String> foodCategoryMap = getCategoryNameMapForFoods(foodMap.values());

        List<GetPreferencesServiceResponse.FoodPreferenceItemDto> liked = foodPreferences.stream()
                .filter(FoodPreference::getIsPreferred)
                .map(fp -> toFoodPreferenceItemDto(fp, foodMap, foodCategoryMap))
                .collect(Collectors.toList());

        List<GetPreferencesServiceResponse.FoodPreferenceItemDto> disliked = foodPreferences.stream()
                .filter(fp -> !fp.getIsPreferred())
                .map(fp -> toFoodPreferenceItemDto(fp, foodMap, foodCategoryMap))
                .collect(Collectors.toList());

        GetPreferencesServiceResponse.FoodPreferencesDto foodPreferencesDto =
                GetPreferencesServiceResponse.FoodPreferencesDto.builder()
                        .liked(liked)
                        .disliked(disliked)
                        .build();

        return GetPreferencesServiceResponse.builder()
                .recommendationType(member.getRecommendationType())
                .categoryPreferences(categoryPreferences)
                .foodPreferences(foodPreferencesDto)
                .build();
    }

    private Map<Long, String> getCategoryNameMap(List<Preference> preferences) {
        List<Long> categoryIds = preferences.stream()
                .map(Preference::getCategoryId)
                .collect(Collectors.toList());

        if (categoryIds.isEmpty()) {
            return Map.of();
        }

        List<Category> categories = categoryRepository.findByIdIn(categoryIds);
        return categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));
    }

    private Map<Long, Food> getFoodMap(List<FoodPreference> foodPreferences) {
        List<Long> foodIds = foodPreferences.stream()
                .map(FoodPreference::getFoodId)
                .collect(Collectors.toList());

        if (foodIds.isEmpty()) {
            return Map.of();
        }

        List<Food> foods = foodRepository.findByIdIn(foodIds);
        return foods.stream()
                .collect(Collectors.toMap(Food::getFoodId, food -> food));
    }

    private Map<Long, String> getCategoryNameMapForFoods(Iterable<Food> foods) {
        List<Long> categoryIds = new ArrayList<>();
        for (Food food : foods) {
            categoryIds.add(food.getCategoryId());
        }
        categoryIds = categoryIds.stream().distinct().collect(Collectors.toList());

        if (categoryIds.isEmpty()) {
            return Map.of();
        }

        List<Category> categories = categoryRepository.findByIdIn(categoryIds);
        return categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));
    }

    private GetPreferencesServiceResponse.FoodPreferenceItemDto toFoodPreferenceItemDto(
            FoodPreference fp,
            Map<Long, Food> foodMap,
            Map<Long, String> categoryMap
    ) {
        Food food = foodMap.get(fp.getFoodId());
        String foodName = food != null ? food.getFoodName() : "알 수 없음";
        String categoryName = food != null ? categoryMap.getOrDefault(food.getCategoryId(), "알 수 없음") : "알 수 없음";

        return GetPreferencesServiceResponse.FoodPreferenceItemDto.builder()
                .foodPreferenceId(fp.getFoodPreferenceId())
                .foodId(fp.getFoodId())
                .foodName(foodName)
                .categoryName(categoryName)
                .createdAt(fp.getPreferredAt())
                .build();
    }
}
