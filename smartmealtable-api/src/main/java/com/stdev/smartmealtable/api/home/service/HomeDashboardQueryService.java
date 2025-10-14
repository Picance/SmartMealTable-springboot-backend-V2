package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.api.home.service.dto.HomeDashboardServiceResponse;
import com.stdev.smartmealtable.core.exception.ResourceNotFoundException;
import com.stdev.smartmealtable.core.exception.error.ErrorType;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MealBudgetRepository;
import com.stdev.smartmealtable.domain.budget.entity.DailyBudget;
import com.stdev.smartmealtable.domain.budget.entity.MealBudget;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.food.entity.Food;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 홈 대시보드 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HomeDashboardQueryService {

    private final AddressHistoryRepository addressHistoryRepository;
    private final DailyBudgetRepository dailyBudgetRepository;
    private final MealBudgetRepository mealBudgetRepository;
    private final ExpenditureRepository expenditureRepository;
    private final FoodRepository foodRepository;
    private final StoreRepository storeRepository;

    /**
     * 홈 대시보드 정보 조회
     *
     * @param memberId 회원 ID
     * @return 홈 대시보드 정보
     */
    public HomeDashboardServiceResponse getHomeDashboard(Long memberId) {
        // 1. 기본 주소 조회 (필수)
        AddressHistory primaryAddress = addressHistoryRepository.findPrimaryByMemberId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorType.ADDRESS_NOT_FOUND));

        // 2. 오늘 날짜
        LocalDate today = LocalDate.now();

        // 3. 오늘의 예산 정보 조회
        DailyBudget dailyBudget = dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, today)
                .orElse(null);

        List<MealBudget> mealBudgets = mealBudgetRepository.findByMemberIdAndBudgetDate(memberId, today);

        // 4. 오늘의 지출 총액 계산
        Long todaySpentLong = expenditureRepository.getTotalAmountByPeriod(memberId, today, today);
        BigDecimal todaySpent = todaySpentLong != null ? BigDecimal.valueOf(todaySpentLong) : BigDecimal.ZERO;

        // 5. 끼니별 지출 계산
        Map<MealType, Long> mealTypeAmounts = expenditureRepository.getAmountByMealTypeForPeriod(memberId, today, today);
        BigDecimal breakfastSpent = BigDecimal.valueOf(mealTypeAmounts.getOrDefault(MealType.BREAKFAST, 0L));
        BigDecimal lunchSpent = BigDecimal.valueOf(mealTypeAmounts.getOrDefault(MealType.LUNCH, 0L));
        BigDecimal dinnerSpent = BigDecimal.valueOf(mealTypeAmounts.getOrDefault(MealType.DINNER, 0L));
        BigDecimal otherSpent = BigDecimal.valueOf(mealTypeAmounts.getOrDefault(MealType.OTHER, 0L));

        // 6. 추천 메뉴 조회 (상위 5개) - 실제 추천 로직은 추후 구현
        List<Food> recommendedFoods = foodRepository.findTop5ByOrderByRegisteredAtDesc();

        // 7. 추천 가게 조회 (상위 5개) - 실제 추천 로직은 추후 구현
        List<Store> recommendedStores = storeRepository.findTop5ByOrderByViewCountDesc();

        // 8. 예산 정보 구성
        BigDecimal todayBudget = dailyBudget != null ? dailyBudget.getTotalBudget() : BigDecimal.ZERO;
        BigDecimal remaining = todayBudget.subtract(todaySpent);
        BigDecimal utilizationRate = todayBudget.compareTo(BigDecimal.ZERO) > 0
                ? todaySpent.divide(todayBudget, 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        // 9. 응답 생성
        return HomeDashboardServiceResponse.of(
                primaryAddress,
                todaySpent,
                todayBudget,
                remaining,
                utilizationRate,
                mealBudgets,
                breakfastSpent,
                lunchSpent,
                dinnerSpent,
                otherSpent,
                recommendedFoods,
                recommendedStores,
                primaryAddress.getLatitude(),
                primaryAddress.getLongitude()
        );
    }
}

