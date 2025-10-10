package com.stdev.smartmealtable.api.member.service.preference;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodPreference;
import com.stdev.smartmealtable.domain.food.FoodPreferenceRepository;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 음식 선호도 추가 Service
 */
@Service
@RequiredArgsConstructor
public class AddFoodPreferenceService {

    private final FoodPreferenceRepository foodPreferenceRepository;
    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public AddFoodPreferenceServiceResponse execute(Long memberId, AddFoodPreferenceServiceRequest request) {
        // 1. Food 존재 여부 확인
        Food food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new BusinessException(ErrorType.FOOD_NOT_FOUND));

        // 2. 중복 확인
        foodPreferenceRepository.findByMemberIdAndFoodId(memberId, request.getFoodId())
                .ifPresent(fp -> {
                    throw new BusinessException(ErrorType.FOOD_PREFERENCE_ALREADY_EXISTS);
                });

        // 3. FoodPreference 생성 및 저장
        FoodPreference foodPreference = FoodPreference.create(memberId, request.getFoodId());
        if (request.getIsPreferred() != null && !request.getIsPreferred()) {
            foodPreference.changePreference(false);
        }
        FoodPreference saved = foodPreferenceRepository.save(foodPreference);

        // 4. Category 정보 조회
        Category category = categoryRepository.findById(food.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorType.CATEGORY_NOT_FOUND));

        // 5. Response 생성
        return new AddFoodPreferenceServiceResponse(
                saved.getFoodPreferenceId(),
                saved.getFoodId(),
                food.getFoodName(),
                category.getName(),
                saved.getIsPreferred(),
                saved.getPreferredAt()
        );
    }
}
