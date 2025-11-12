package com.stdev.smartmealtable.api.expenditure.service;

import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceRequest;
import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MealBudget;
import com.stdev.smartmealtable.domain.budget.MealBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import com.stdev.smartmealtable.domain.budget.MonthlyBudgetRepository;
import com.stdev.smartmealtable.domain.expenditure.service.ExpenditureDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 내역 등록 Application Service
 * 유즈케이스 orchestration에 집중
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreateExpenditureService {

    private final ExpenditureDomainService expenditureDomainService;
    private final DailyBudgetRepository dailyBudgetRepository;
    private final MealBudgetRepository mealBudgetRepository;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    
    /**
     * 지출 내역 등록
     * Domain Service를 통한 비즈니스 로직 처리
     */
    @Transactional
    public CreateExpenditureServiceResponse createExpenditure(CreateExpenditureServiceRequest request) {
        // storeId 여부에 따라 다른 Domain Service 메서드 호출
        if (request.storeId() != null) {
            // 장바구니 시나리오: storeId + foodId 포함
            List<ExpenditureDomainService.CartExpenditureItemRequest> cartItems = request.items() != null
                    ? request.items().stream()
                    .map(itemReq -> new ExpenditureDomainService.CartExpenditureItemRequest(
                            itemReq.foodId(),
                            itemReq.foodName(),  // foodName도 함께 전달
                            itemReq.quantity(),
                            itemReq.price()
                    ))
                    .collect(Collectors.toList())
                    : List.of();

            ExpenditureDomainService.ExpenditureCreationResult result = expenditureDomainService.createExpenditureFromCart(
                    request.memberId(),
                    request.storeId(),
                    request.storeName(),
                    request.amount(),
                    request.expendedDate(),
                    request.expendedTime(),
                    request.categoryId(),
                    request.mealType(),
                    request.memo(),
                    request.discount(),  // 할인액 전달
                    cartItems
            );

            var response = CreateExpenditureServiceResponse.from(result.expenditure(), result.categoryName());
            updateBudgetUsedAmounts(request.memberId(), request.amount(), request.expendedDate(), request.mealType());
            return response;
        } else {
            // 수기 입력 시나리오: storeId 없음
            List<ExpenditureDomainService.ManualExpenditureItemRequest> itemRequests = request.items() != null
                    ? request.items().stream()
                    .map(itemReq -> new ExpenditureDomainService.ManualExpenditureItemRequest(
                            itemReq.foodName(),
                            itemReq.quantity(),
                            itemReq.price()
                    ))
                    .collect(Collectors.toList())
                    : List.of();

            ExpenditureDomainService.ExpenditureCreationResult result = expenditureDomainService.createExpenditureFromManualInput(
                    request.memberId(),
                    request.storeName(),
                    request.amount(),
                    request.expendedDate(),
                    request.expendedTime(),
                    request.categoryId(),
                    request.mealType(),
                    request.memo(),
                    request.discount(),  // 할인액 전달
                    itemRequests
            );

            var response = CreateExpenditureServiceResponse.from(result.expenditure(), result.categoryName());
            updateBudgetUsedAmounts(request.memberId(), request.amount(), request.expendedDate(), request.mealType());
            return response;
        }
    }

    /**
     * 지출 생성 후 예산 사용액 업데이트
     * 일별, 월별, 식사별 예산의 사용액을 증가시킵니다
     */
    private void updateBudgetUsedAmounts(Long memberId, Integer amount, java.time.LocalDate expendedDate, com.stdev.smartmealtable.domain.expenditure.MealType mealType) {
        if (amount == null || amount <= 0) {
            return;
        }

        // 일일 예산 업데이트
        DailyBudget dailyBudget = dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, expendedDate)
                .orElseThrow(() -> new BusinessException(ErrorType.DAILY_BUDGET_NOT_FOUND));
        dailyBudget.addUsedAmount(amount);
        dailyBudgetRepository.save(dailyBudget);

        // 식사별 예산 업데이트 (mealType이 null이 아닐 경우만 처리)
        if (mealType != null) {
            MealBudget mealBudget = mealBudgetRepository.findByDailyBudgetIdAndMealType(dailyBudget.getBudgetId(), mealType);
            if (mealBudget != null) {
                mealBudget.addUsedAmount(amount);
                mealBudgetRepository.save(mealBudget);
            }
        }

        // 월별 예산 업데이트
        YearMonth expenditureMonth = YearMonth.from(expendedDate);
        String budgetMonth = String.format("%04d-%02d", expenditureMonth.getYear(), expenditureMonth.getMonthValue());
        MonthlyBudget monthlyBudget = monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, budgetMonth)
                .orElseThrow(() -> new BusinessException(ErrorType.MONTHLY_BUDGET_NOT_FOUND));
        monthlyBudget.addUsedAmount(amount);
        monthlyBudgetRepository.save(monthlyBudget);
    }
}
