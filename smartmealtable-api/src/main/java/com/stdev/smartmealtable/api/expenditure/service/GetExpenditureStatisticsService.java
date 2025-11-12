package com.stdev.smartmealtable.api.expenditure.service;

import com.stdev.smartmealtable.api.budget.service.DailyBudgetQueryService;
import com.stdev.smartmealtable.api.budget.service.dto.DailyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureStatisticsServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 지출 통계 조회 Application Service
 * 캘린더 뷰용 일별 지출액과 예산 정보를 함께 조회합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetExpenditureStatisticsService {

    private final ExpenditureRepository expenditureRepository;
    private final CategoryRepository categoryRepository;
    private final DailyBudgetQueryService dailyBudgetQueryService;

    /**
     * 지출 통계 조회 (캘린더 뷰용)
     * 각 날짜별로 예산과 지출액을 비교하여 초과/절약 여부를 반환합니다.
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
        Map<LocalDate, Long> dailyExpenditureAmounts = expenditureRepository.getDailyAmountForPeriod(memberId, startDate, endDate);

        // 4. 일별 예산 정보를 조회하여 balance 계산
        List<ExpenditureStatisticsServiceResponse.DailyStatistics> dailyStatistics = buildDailyStatistics(
                memberId,
                startDate,
                endDate,
                dailyExpenditureAmounts
        );

        // 5. 식사 유형별 지출 금액 집계 조회
        Map<MealType, Long> mealTypeStatistics = expenditureRepository.getAmountByMealTypeForPeriod(memberId, startDate, endDate);

        return new ExpenditureStatisticsServiceResponse(
                totalAmount,
                categoryStatistics,
                dailyStatistics,
                mealTypeStatistics
        );
    }

    /**
     * 일별 통계 구축 (예산 정보 포함)
     * 각 날짜에 대해 예산과 지출액을 비교하여 balance와 overBudget을 계산합니다.
     */
    private List<ExpenditureStatisticsServiceResponse.DailyStatistics> buildDailyStatistics(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate,
            Map<LocalDate, Long> dailyExpenditureAmounts
    ) {
        List<ExpenditureStatisticsServiceResponse.DailyStatistics> result = new ArrayList<>();

        // startDate부터 endDate까지 모든 날짜 순회
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            long spentAmount = dailyExpenditureAmounts.getOrDefault(currentDate, 0L);

            try {
                // 해당 날짜의 일일 예산 조회
                DailyBudgetQueryServiceResponse budgetInfo = dailyBudgetQueryService.getDailyBudget(memberId, currentDate);
                long budget = budgetInfo.totalBudget() != null ? budgetInfo.totalBudget().longValue() : 0L;
                long balance = budget - spentAmount;
                boolean overBudget = spentAmount > budget;

                result.add(new ExpenditureStatisticsServiceResponse.DailyStatistics(
                        currentDate,
                        spentAmount,
                        budget,
                        balance,
                        overBudget
                ));
            } catch (BusinessException e) {
                // 예산이 설정되지 않은 날짜는 0으로 처리
                log.debug("예산이 설정되지 않은 날짜: {}", currentDate);
                result.add(new ExpenditureStatisticsServiceResponse.DailyStatistics(
                        currentDate,
                        spentAmount,
                        0L,
                        -spentAmount,
                        spentAmount > 0
                ));
            }

            currentDate = currentDate.plusDays(1);
        }

        return result;
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
