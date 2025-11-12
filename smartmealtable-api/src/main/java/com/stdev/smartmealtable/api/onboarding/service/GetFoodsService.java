package com.stdev.smartmealtable.api.onboarding.service;

import com.stdev.smartmealtable.api.onboarding.service.dto.GetFoodsServiceResponse;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 온보딩 - 음식 목록 조회 Application Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetFoodsService {

    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 온보딩용 음식 목록 조회 (다양한 카테고리에서 랜덤 조회)
     *
     * 온보딩 과정에서 사용자가 다양한 카테고리의 음식을 보고 선택하도록 하여
     * 사용자의 실제 선호도를 파악합니다. 따라서 모든 카테고리에서 랜덤하게 음식을 제공합니다.
     *
     * @param page 페이지 번호
     * @param size 페이지 크기 (한 번에 조회할 음식 개수)
     * @return 다양한 카테고리에서 선택된 음식 목록
     */
    public GetFoodsServiceResponse getFoods(int page, int size) {
        log.info("온보딩 음식 목록 조회 (랜덤) - page: {}, size: {}", page, size);

        // 1. 랜덤 음식 목록 조회
        List<Food> foods = foodRepository.findRandom(page, size);
        long totalElements = foodRepository.count();

        // 2. 카테고리 정보 조회 (한 번에 조회하여 성능 최적화)
        Map<Long, String> categoryNameMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));

        // 3. 응답 생성
        List<GetFoodsServiceResponse.FoodInfo> foodInfos = foods.stream()
                .map(food -> new GetFoodsServiceResponse.FoodInfo(
                        food.getFoodId(),
                        food.getFoodName(),
                        food.getCategoryId(),
                        categoryNameMap.get(food.getCategoryId()),
                        food.getImageUrl(),
                        food.getDescription(),
                        food.getAveragePrice()
                ))
                .collect(Collectors.toList());

        // 4. 페이징 정보 계산
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isLast = page >= totalPages - 1;
        boolean isFirst = page == 0;

        return new GetFoodsServiceResponse(
                foodInfos,
                page,
                size,
                totalElements,
                totalPages,
                isLast,
                isFirst
        );
    }
}
