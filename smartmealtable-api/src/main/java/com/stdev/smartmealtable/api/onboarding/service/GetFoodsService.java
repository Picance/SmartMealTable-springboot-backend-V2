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
     * 온보딩용 음식 목록 조회 (페이징)
     */
    public GetFoodsServiceResponse getFoods(Long categoryId, int page, int size) {
        log.info("온보딩 음식 목록 조회 - categoryId: {}, page: {}, size: {}", categoryId, page, size);

        // 1. 음식 목록 조회
        List<Food> foods;
        long totalElements;

        if (categoryId != null) {
            foods = foodRepository.findByCategoryId(categoryId, page, size);
            totalElements = foodRepository.countByCategoryId(categoryId);
        } else {
            foods = foodRepository.findAll(page, size);
            totalElements = foodRepository.count();
        }

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
