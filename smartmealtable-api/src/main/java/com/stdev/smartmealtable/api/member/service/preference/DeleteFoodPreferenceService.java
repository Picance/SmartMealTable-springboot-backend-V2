package com.stdev.smartmealtable.api.member.service.preference;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.food.FoodPreference;
import com.stdev.smartmealtable.domain.food.FoodPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 음식 선호도 삭제 Service
 */
@Service
@RequiredArgsConstructor
public class DeleteFoodPreferenceService {

    private final FoodPreferenceRepository foodPreferenceRepository;

    @Transactional
    public void execute(Long memberId, Long foodPreferenceId) {
        // 1. FoodPreference 조회 및 권한 검증
        FoodPreference foodPreference = foodPreferenceRepository.findById(foodPreferenceId)
                .orElseThrow(() -> new BusinessException(ErrorType.FOOD_PREFERENCE_NOT_FOUND));

        if (!foodPreference.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorType.FORBIDDEN_ACCESS);
        }

        // 2. 삭제
        foodPreferenceRepository.deleteById(foodPreferenceId);
    }
}
