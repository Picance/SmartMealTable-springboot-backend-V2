package com.stdev.smartmealtable.api.onboarding.service;

import com.stdev.smartmealtable.api.onboarding.service.dto.SetBudgetServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.SetBudgetServiceResponse;
import com.stdev.smartmealtable.domain.budget.service.BudgetDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 온보딩 - 예산 설정 Application Service
 * 유즈케이스 orchestration에 집중
 */
@Service
@RequiredArgsConstructor
public class SetBudgetService {

    private final BudgetDomainService budgetDomainService;

    /**
     * 회원의 예산 설정
     * Domain Service를 통한 비즈니스 로직 처리
     * - 현재 월의 월별 예산 생성
     * - 오늘 날짜의 일일 예산 생성
     * - 오늘 날짜의 식사별 예산 생성 (아침, 점심, 저녁)
     */
    @Transactional
    public SetBudgetServiceResponse setBudget(Long memberId, SetBudgetServiceRequest request) {
        // Domain Service를 통한 초기 예산 설정 (도메인 로직 포함)
        BudgetDomainService.BudgetSetupResult result = budgetDomainService.setupInitialBudget(
                memberId,
                request.getMonthlyBudget(),
                request.getDailyBudget(),
                request.getMealBudgets()
        );

        // 응답 DTO 생성
        List<SetBudgetServiceResponse.MealBudgetInfo> mealBudgetInfos = result.mealBudgets().stream()
                .map(mb -> new SetBudgetServiceResponse.MealBudgetInfo(
                        mb.getMealType(),
                        mb.getMealBudget()
                ))
                .collect(Collectors.toList());

        return new SetBudgetServiceResponse(
                result.monthlyBudget().getMonthlyFoodBudget(),
                result.dailyBudget().getDailyFoodBudget(),
                mealBudgetInfos
        );
    }
}
