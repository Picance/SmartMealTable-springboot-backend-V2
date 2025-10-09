package com.stdev.smartmealtable.api.onboarding.service;

import com.stdev.smartmealtable.api.onboarding.service.dto.SetBudgetServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.SetBudgetServiceResponse;
import com.stdev.smartmealtable.domain.budget.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 온보딩 - 예산 설정 Application Service
 */
@Service
@RequiredArgsConstructor
public class SetBudgetService {

    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final DailyBudgetRepository dailyBudgetRepository;
    private final MealBudgetRepository mealBudgetRepository;

    /**
     * 회원의 예산 설정
     * - 현재 월의 월별 예산 생성
     * - 오늘 날짜의 일일 예산 생성
     * - 오늘 날짜의 식사별 예산 생성 (아침, 점심, 저녁)
     */
    @Transactional
    public SetBudgetServiceResponse setBudget(Long memberId, SetBudgetServiceRequest request) {
        // 1. 현재 년월 및 날짜 계산
        YearMonth currentMonth = YearMonth.now();
        String budgetMonth = currentMonth.toString(); // "YYYY-MM"
        LocalDate today = LocalDate.now();

        // 2. 월별 예산 생성
        MonthlyBudget monthlyBudget = MonthlyBudget.create(
                memberId,
                request.getMonthlyBudget(),
                budgetMonth
        );
        monthlyBudget = monthlyBudgetRepository.save(monthlyBudget);

        // 3. 일일 예산 생성
        DailyBudget dailyBudget = DailyBudget.create(
                memberId,
                request.getDailyBudget(),
                today
        );
        dailyBudget = dailyBudgetRepository.save(dailyBudget);

        // 4. 식사별 예산 생성
        List<SetBudgetServiceResponse.MealBudgetInfo> mealBudgetInfos = new ArrayList<>();
        for (Map.Entry<MealType, Integer> entry : request.getMealBudgets().entrySet()) {
            MealBudget mealBudget = MealBudget.create(
                    dailyBudget.getBudgetId(),
                    entry.getValue(),
                    entry.getKey(),
                    today
            );
            mealBudgetRepository.save(mealBudget);
            
            mealBudgetInfos.add(new SetBudgetServiceResponse.MealBudgetInfo(
                    entry.getKey(),
                    entry.getValue()
            ));
        }

        // 5. 응답 생성
        return new SetBudgetServiceResponse(
                monthlyBudget.getMonthlyFoodBudget(),
                dailyBudget.getDailyFoodBudget(),
                mealBudgetInfos
        );
    }
}
