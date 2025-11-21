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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
        int pageNumber = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 1), 100);

        log.info("온보딩 음식 목록 조회 (랜덤) - page: {}, size: {}", pageNumber, pageSize);

        // 1. 랜덤 음식 목록 조회 (이미지 필수, 삭제 제외)
        List<Food> foods = foodRepository.findRandom(pageNumber, pageSize);
        long totalElements = foodRepository.countOnboardingCandidates();

        // 2. 카테고리 정보 조회 (한 번에 조회하여 성능 최적화)
        Map<Long, String> categoryNameMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));

        // 3. 카테고리 다양성 확보를 위해 카테고리별로 라운드로빈 정렬
        List<Food> diversifiedFoods = diversifyByCategory(foods);

        // 4. 응답 생성
        List<GetFoodsServiceResponse.FoodInfo> foodInfos = diversifiedFoods.stream()
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

        // 5. 페이징 정보 계산 (온보딩 대상 음식만 기준)
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isLast = totalPages == 0 || pageNumber >= totalPages - 1;
        boolean isFirst = pageNumber == 0;

        return new GetFoodsServiceResponse(
                foodInfos,
                pageNumber,
                pageSize,
                totalElements,
                totalPages,
                isLast,
                isFirst
        );
    }

    /**
     * 다양한 카테고리 노출을 위해 카테고리별 라운드로빈으로 재정렬
     */
    private List<Food> diversifyByCategory(List<Food> foods) {
        if (foods.isEmpty()) {
            return foods;
        }

        List<Food> shuffled = new ArrayList<>(foods);
        Collections.shuffle(shuffled);

        Map<Long, Queue<Food>> grouped = shuffled.stream()
                .collect(Collectors.groupingBy(
                        Food::getCategoryId,
                        Collectors.toCollection(ArrayDeque::new)
                ));

        List<Food> diversified = new ArrayList<>(shuffled.size());
        List<Long> categoryCycle = new ArrayList<>(grouped.keySet());

        while (!categoryCycle.isEmpty()) {
            Iterator<Long> iterator = categoryCycle.iterator();
            while (iterator.hasNext()) {
                Long categoryId = iterator.next();
                Queue<Food> queue = grouped.get(categoryId);
                if (queue == null || queue.isEmpty()) {
                    iterator.remove();
                    continue;
                }

                diversified.add(queue.poll());

                if (queue.isEmpty()) {
                    iterator.remove();
                }
            }
        }

        return diversified;
    }
}
