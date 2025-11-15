package com.stdev.smartmealtable.domain.budget.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.budget.*;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 예산 도메인 서비스
 * 예산 생성, 수정 등 핵심 비즈니스 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetDomainService {

    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final DailyBudgetRepository dailyBudgetRepository;
    private final MealBudgetRepository mealBudgetRepository;

    /**
     * 온보딩 시 초기 예산 설정
     * - 현재 월의 월별 예산 생성
     * - 오늘부터 월말까지 모든 날짜에 대한 일일 예산 생성
     * - 오늘부터 월말까지 모든 날짜에 대한 식사별 예산 생성 (아침, 점심, 저녁)
     *
     * @param memberId      회원 ID
     * @param monthlyAmount 월별 예산 금액
     * @param dailyAmount   일일 예산 금액
     * @param mealBudgets   식사별 예산 (MealType → 금액)
     * @return 생성된 월별, 일일, 식사별 예산 객체들 (첫 번째 날짜의 일일/식사별 예산 반환)
     */
    public BudgetSetupResult setupInitialBudget(
            Long memberId,
            Integer monthlyAmount,
            Integer dailyAmount,
            Map<MealType, Integer> mealBudgets
    ) {
        // 1. 현재 년월 및 날짜 계산
        YearMonth currentMonth = YearMonth.now();
        String budgetMonth = currentMonth.toString(); // "YYYY-MM"
        LocalDate today = LocalDate.now();
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, budgetMonth)
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorType.MONTHLY_BUDGET_ALREADY_EXISTS);
                });

        // 2. 월별 예산 생성
        MonthlyBudget monthlyBudget = MonthlyBudget.create(
                memberId,
                monthlyAmount,
                budgetMonth
        );

        try {
            monthlyBudget = monthlyBudgetRepository.save(monthlyBudget);
        } catch (DataIntegrityViolationException ex) {
            if (isMonthlyBudgetUniqueConstraintViolation(ex)) {
                throw new BusinessException(ErrorType.MONTHLY_BUDGET_ALREADY_EXISTS);
            }
            throw ex;
        }

        DailyBudgetBatchResult dailyBudgetBatchResult = createDailyBudgetsInRange(
                memberId,
                today,
                endOfMonth,
                dailyAmount,
                mealBudgets
        );

        DailyBudget firstDailyBudget = dailyBudgetBatchResult.dailyBudgets().isEmpty()
                ? null
                : dailyBudgetBatchResult.dailyBudgets().get(0);
        List<MealBudget> allMealBudgets = dailyBudgetBatchResult.mealBudgets();

        long dayCount = java.time.temporal.ChronoUnit.DAYS.between(today, endOfMonth) + 1;
        log.info("초기 예산 설정 완료 - memberId: {}, monthly: {}, daily: {}, meals: {}, days: {} (오늘 ~ 월말)",
                memberId, monthlyAmount, dailyAmount, mealBudgets.size(), dayCount);

        return new BudgetSetupResult(monthlyBudget, firstDailyBudget, allMealBudgets);
    }

    /**
     * 월별 예산 수정
     *
     * @param memberId 회원 ID
     * @param month 예산 월 (YYYY-MM)
     * @param newAmount 새 월별 예산 금액
     * @return 수정된 월별 예산
     */
    public MonthlyBudget updateMonthlyBudget(Long memberId, String month, Integer newAmount) {
        MonthlyBudget monthlyBudget = monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, month)
                .orElseThrow(() -> new BusinessException(ErrorType.MONTHLY_BUDGET_NOT_FOUND));

        monthlyBudget.changeMonthlyFoodBudget(newAmount);
        return monthlyBudgetRepository.save(monthlyBudget);
    }

    /**
     * 특정 월의 모든 일일 예산 일괄 수정
     *
     * @param memberId 회원 ID
     * @param month 예산 월 (YYYY-MM)
     * @param newAmount 새 일일 예산 금액
     * @return 수정된 일일 예산 리스트
     */
    public List<DailyBudget> updateDailyBudgetsInMonth(Long memberId, String month, Integer newAmount) {
        // 해당 월의 시작일과 종료일 계산
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        // 해당 월의 모든 일일 예산 조회
        List<DailyBudget> dailyBudgets = dailyBudgetRepository.findByMemberIdAndBudgetDateGreaterThanEqual(memberId, startOfMonth);

        // 해당 월의 일일 예산만 필터링하여 수정
        List<DailyBudget> updatedBudgets = dailyBudgets.stream()
                .filter(budget -> !budget.getBudgetDate().isAfter(endOfMonth))
                .peek(budget -> budget.changeDailyFoodBudget(newAmount))
                .map(dailyBudgetRepository::save)
                .toList();

        log.info("일일 예산 일괄 수정 완료 - memberId: {}, month: {}, count: {}, amount: {}",
                memberId, month, updatedBudgets.size(), newAmount);

        return updatedBudgets;
    }

    /**
     * 특정 기간의 일일 예산 및 식사별 예산 생성
     */
    public DailyBudgetBatchResult createDailyBudgetsInRange(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate,
            Integer dailyAmount,
            Map<MealType, Integer> mealBudgets
    ) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorType.INVALID_DATE_RANGE);
        }

        // 기간 내 중복 예산 존재 여부 확인
        List<DailyBudget> existingBudgets = dailyBudgetRepository.findByMemberIdAndBudgetDateGreaterThanEqual(memberId, startDate)
                .stream()
                .filter(budget -> !budget.getBudgetDate().isAfter(endDate))
                .toList();

        if (!existingBudgets.isEmpty()) {
            throw new BusinessException(ErrorType.DAILY_BUDGET_ALREADY_EXISTS);
        }

        List<DailyBudget> createdDailyBudgets = new ArrayList<>();
        List<MealBudget> createdMealBudgets = new ArrayList<>();

        try {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                DailyBudget dailyBudget = DailyBudget.create(memberId, dailyAmount, date);
                DailyBudget savedDailyBudget = dailyBudgetRepository.save(dailyBudget);
                createdDailyBudgets.add(savedDailyBudget);

                if (mealBudgets != null && !mealBudgets.isEmpty()) {
                    for (Map.Entry<MealType, Integer> entry : mealBudgets.entrySet()) {
                        MealBudget mealBudget = MealBudget.create(
                                savedDailyBudget.getBudgetId(),
                                entry.getValue(),
                                entry.getKey(),
                                date
                        );
                        MealBudget savedMealBudget = mealBudgetRepository.save(mealBudget);
                        createdMealBudgets.add(savedMealBudget);
                    }
                }
            }
        } catch (DataIntegrityViolationException ex) {
            if (isDailyBudgetUniqueConstraintViolation(ex)) {
                throw new BusinessException(ErrorType.DAILY_BUDGET_ALREADY_EXISTS);
            }
            throw ex;
        }

        log.info("일일 예산 일괄 생성 완료 - memberId: {}, start: {}, end: {}, count: {}",
                memberId, startDate, endDate, createdDailyBudgets.size());

        return new DailyBudgetBatchResult(createdDailyBudgets, createdMealBudgets);
    }

    private boolean isMonthlyBudgetUniqueConstraintViolation(DataIntegrityViolationException ex) {
        return containsConstraintName(ex, "uq_monthly_budget_member_month");
    }

    private boolean isDailyBudgetUniqueConstraintViolation(DataIntegrityViolationException ex) {
        return containsConstraintName(ex, "uq_daily_budget_member_date");
    }

    private boolean containsConstraintName(DataIntegrityViolationException ex, String constraintName) {
        Throwable root = ex.getMostSpecificCause();
        String message = root != null ? root.getMessage() : ex.getMessage();
        return message != null && message.contains(constraintName);
    }

    /**
     * 예산 설정 결과 DTO
     */
    public record BudgetSetupResult(
            MonthlyBudget monthlyBudget,
            DailyBudget dailyBudget,
            List<MealBudget> mealBudgets
    ) {
    }

    /**
     * 일괄 생성 결과 DTO
     */
    public record DailyBudgetBatchResult(
            List<DailyBudget> dailyBudgets,
            List<MealBudget> mealBudgets
    ) {
    }
}
