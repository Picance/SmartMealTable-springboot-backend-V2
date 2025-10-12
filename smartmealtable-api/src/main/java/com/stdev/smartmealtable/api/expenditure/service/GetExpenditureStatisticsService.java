package com.stdev.smartmealtable.api.expenditure.service;

import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureStatisticsServiceResponse;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 지출 통계 조회 Application Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetExpenditureStatisticsService {
    
    private final ExpenditureRepository expenditureRepository;
    private final CategoryRepository categoryRepository;
    
    /**
     * 지출 통계 조회
     */
    public ExpenditureStatisticsServiceResponse getStatistics(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("지출 통계 조회 - memberId: {}, startDate: {}, endDate: {}", memberId, startDate, endDate);
        
        // 1. 총 지출 금액 조회
        Long totalAmount = expenditureRepository.getTotalAmountByPeriod(memberId, startDate, endDate);
        
        // 2. 카테고리별 지출 금액 집계 조회
        Map<Long, Long> categoryAmounts = expenditureRepository.getAmountByCategoryForPeriod(memberId, startDate, endDate);
        Map<Long, ExpenditureStatisticsServiceResponse.CategoryStatistics> categoryStatistics = 
                buildCategoryStatistics(categoryAmounts);
        
        // 3. 일별 지출 금액 집계 조회
        Map<LocalDate, Long> dailyStatistics = expenditureRepository.getDailyAmountForPeriod(memberId, startDate, endDate);
        
        // 4. 식사 유형별 지출 금액 집계 조회
        Map<MealType, Long> mealTypeStatistics = expenditureRepository.getAmountByMealTypeForPeriod(memberId, startDate, endDate);
        
        return new ExpenditureStatisticsServiceResponse(
                totalAmount,
                categoryStatistics,
                dailyStatistics,
                mealTypeStatistics
        );
    }
    
    /**
     * 카테고리별 통계 구축 (카테고리 이름 포함)
     */
    private Map<Long, ExpenditureStatisticsServiceResponse.CategoryStatistics> buildCategoryStatistics(
            Map<Long, Long> categoryAmounts
    ) {
        if (categoryAmounts.isEmpty()) {
            return Map.of();
        }
        
        // 카테고리 ID 목록으로 카테고리 정보 조회
        List<Long> categoryIds = categoryAmounts.keySet().stream().toList();
        List<Category> categories = categoryRepository.findByIdIn(categoryIds);
        
        // 카테고리 ID -> Category 매핑
        Map<Long, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, category -> category));
        
        // 통계 구축
        Map<Long, ExpenditureStatisticsServiceResponse.CategoryStatistics> result = new HashMap<>();
        for (Map.Entry<Long, Long> entry : categoryAmounts.entrySet()) {
            Long categoryId = entry.getKey();
            Long amount = entry.getValue();
            Category category = categoryMap.get(categoryId);
            
            String categoryName = category != null ? category.getName() : "알 수 없음";
            
            result.put(categoryId, new ExpenditureStatisticsServiceResponse.CategoryStatistics(
                    categoryId,
                    categoryName,
                    amount
            ));
        }
        
        return result;
    }
}
