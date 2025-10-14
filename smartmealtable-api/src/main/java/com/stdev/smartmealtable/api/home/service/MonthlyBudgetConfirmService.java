package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.api.home.service.dto.MonthlyBudgetConfirmServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.core.exception.ResourceNotFoundException;
import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import com.stdev.smartmealtable.domain.budget.MonthlyBudgetRepository;
import com.stdev.smartmealtable.domain.member.entity.BudgetConfirmAction;
import com.stdev.smartmealtable.domain.member.entity.MonthlyBudgetConfirmation;
import com.stdev.smartmealtable.domain.member.repository.MonthlyBudgetConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 월별 예산 확인 처리 서비스
 */
@Service
@RequiredArgsConstructor
public class MonthlyBudgetConfirmService {

    private final MonthlyBudgetConfirmationRepository confirmationRepository;
    private final MonthlyBudgetRepository monthlyBudgetRepository;

    /**
     * 월별 예산 확인 처리
     *
     * @param memberId 회원 ID
     * @param year     연도
     * @param month    월 (1-12)
     * @param action   사용자 액션 (KEEP/CHANGE)
     * @return 확인 처리 결과
     */
    @Transactional
    public MonthlyBudgetConfirmServiceResponse confirmMonthlyBudget(
            Long memberId,
            Integer year,
            Integer month,
            String action
    ) {
        // 1. 중복 확인 체크
        if (confirmationRepository.existsByMemberIdAndYearAndMonth(memberId, year, month)) {
            throw new BusinessException(ErrorType.ALREADY_CONFIRMED_MONTHLY_BUDGET);
        }

        // 2. 월별 예산 조회 (budgetMonth 형식: "YYYY-MM")
        String budgetMonth = String.format("%d-%02d", year, month);
        MonthlyBudget monthlyBudget = monthlyBudgetRepository
                .findByMemberIdAndBudgetMonth(memberId, budgetMonth)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorType.MONTHLY_BUDGET_NOT_FOUND));

        // 3. 액션 Enum 변환
        BudgetConfirmAction budgetAction;
        try {
            budgetAction = BudgetConfirmAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorType.INVALID_BUDGET_CONFIRM_ACTION);
        }

        // 4. 확인 이력 생성
        MonthlyBudgetConfirmation confirmation = MonthlyBudgetConfirmation.create(
                memberId,
                year,
                month,
                budgetAction
        );

        // 5. 저장
        MonthlyBudgetConfirmation saved = confirmationRepository.save(confirmation);

        // 6. 응답 생성
        return MonthlyBudgetConfirmServiceResponse.of(
                saved,
                monthlyBudget.getMonthlyFoodBudget()
        );
    }
}
