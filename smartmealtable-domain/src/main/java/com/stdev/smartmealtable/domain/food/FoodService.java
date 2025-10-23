package com.stdev.smartmealtable.domain.food;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 음식 도메인 서비스
 * 음식 관련 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;

    /**
     * 음식 ID로 조회
     * 
     * @param foodId 음식 ID
     * @return 음식 도메인 엔티티
     */
    public Optional<Food> findById(Long foodId) {
        return foodRepository.findById(foodId);
    }

    /**
     * 음식이 존재하는지 확인
     * 
     * @param foodId 음식 ID
     * @return 존재 여부
     */
    public boolean exists(Long foodId) {
        return foodRepository.findById(foodId).isPresent();
    }
}
