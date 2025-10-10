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
 * 음식 선호도 변경 Service
 */
@Service
@RequiredArgsConstructor
public class UpdateFoodPreferenceService {

    private final FoodPreferenceRepository foodPreferenceRepository;
    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public UpdateFoodPreferenceServiceResponse execute(
            Long memberId,
            Long foodPreferenceId,
            UpdateFoodPreferenceServiceRequest request
    ) {
        // 1. FoodPreference 조회 및 권한 검증
        FoodPreference foodPreference = foodPreferenceRepository.findById(foodPreferenceId)
                .orElseThrow(() -> new BusinessException(ErrorType.FOOD_PREFERENCE_NOT_FOUND));

        if (!foodPreference.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorType.FORBIDDEN_ACCESS);
        }

        // 2. 선호도 변경
        foodPreference.changePreference(request.getIsPreferred());
        FoodPreference updated = foodPreferenceRepository.save(foodPreference);

        // 3. Food 및 Category 정보 조회
        Food food = foodRepository.findById(updated.getFoodId())
                .orElseThrow(() -> new BusinessException(ErrorType.FOOD_NOT_FOUND));

        Category category = categoryRepository.findById(food.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorType.CATEGORY_NOT_FOUND));

        // 4. Response 생성
        return new UpdateFoodPreferenceServiceResponse(
                updated.getFoodPreferenceId(),
                updated.getFoodId(),
                food.getFoodName(),
                category.getName(),
                updated.getIsPreferred(),
                updated.getPreferredAt()
        );
    }
}
