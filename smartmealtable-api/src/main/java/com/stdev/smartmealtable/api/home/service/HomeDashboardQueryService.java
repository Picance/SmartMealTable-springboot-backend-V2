package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.api.home.service.dto.HomeDashboardServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.ResourceNotFoundException;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MealBudget;
import com.stdev.smartmealtable.domain.budget.MealBudgetRepository;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 홈 대시보드 조회 서비스
 *
 * 사용자의 홈 대시보드 정보를 조회합니다:
 * - 위치 정보
 * - 예산 정보
 * - 추천 메뉴
 * - 추천 가게
 *
 * @author SmartMealTable Team
 * @since 2025-11-12
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class HomeDashboardQueryService {

    private final AddressHistoryRepository addressHistoryRepository;
    private final DailyBudgetRepository dailyBudgetRepository;
    private final MealBudgetRepository mealBudgetRepository;
    private final ExpenditureRepository expenditureRepository;
    private final DashboardRecommendationService dashboardRecommendationService;

    private static final int DEFAULT_RECOMMENDATION_LIMIT = 5;

    /**
     * 홈 대시보드 조회 (위치 정보 없이)
     *
     * @param memberId 사용자 ID
     * @return 홈 대시보드 정보
     */
    public HomeDashboardServiceResponse getHomeDashboard(Long memberId) {
        return getHomeDashboardWithLocation(memberId, null, null);
    }

    /**
     * 홈 대시보드 조회 (위치 정보 포함)
     *
     * @param memberId 사용자 ID
     * @param userLatitude 사용자 위도
     * @param userLongitude 사용자 경도
     * @return 홈 대시보드 정보
     */
    public HomeDashboardServiceResponse getHomeDashboardWithLocation(
            Long memberId,
            Double userLatitude,
            Double userLongitude
    ) {
        log.debug("Getting home dashboard for member {}, location: ({}, {})",
                memberId, userLatitude, userLongitude);

        AddressHistory primaryAddress = addressHistoryRepository.findPrimaryByMemberId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorType.ADDRESS_NOT_FOUND));

        Double primaryLatitude = primaryAddress.getAddress().getLatitude();
        Double primaryLongitude = primaryAddress.getAddress().getLongitude();
        boolean hasCustomLocation = userLatitude != null && userLongitude != null;
        Double recommendationLatitude = hasCustomLocation ? userLatitude : primaryLatitude;
        Double recommendationLongitude = hasCustomLocation ? userLongitude : primaryLongitude;

        LocalDate today = LocalDate.now();

        DailyBudget dailyBudget = dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, today)
                .orElse(null);

        List<MealBudget> mealBudgets = mealBudgetRepository.findByMemberIdAndBudgetDate(memberId, today);

        Long todaySpentLong = expenditureRepository.getTotalAmountByPeriod(memberId, today, today);
        BigDecimal todaySpent = todaySpentLong != null ? BigDecimal.valueOf(todaySpentLong) : BigDecimal.ZERO;

        // 식사 유형별 지출 조회
        Map<MealType, Long> mealTypeSpent = expenditureRepository.getAmountByMealTypeForPeriod(memberId, today, today);

        BigDecimal breakfastSpent = mealTypeSpent.containsKey(MealType.BREAKFAST)
                ? BigDecimal.valueOf(mealTypeSpent.get(MealType.BREAKFAST))
                : BigDecimal.ZERO;
        BigDecimal lunchSpent = mealTypeSpent.containsKey(MealType.LUNCH)
                ? BigDecimal.valueOf(mealTypeSpent.get(MealType.LUNCH))
                : BigDecimal.ZERO;
        BigDecimal dinnerSpent = mealTypeSpent.containsKey(MealType.DINNER)
                ? BigDecimal.valueOf(mealTypeSpent.get(MealType.DINNER))
                : BigDecimal.ZERO;
        BigDecimal otherSpent = mealTypeSpent.containsKey(MealType.OTHER)
                ? BigDecimal.valueOf(mealTypeSpent.get(MealType.OTHER))
                : BigDecimal.ZERO;

        BigDecimal todayBudget = dailyBudget != null ? BigDecimal.valueOf(dailyBudget.getDailyFoodBudget()) : BigDecimal.ZERO;

        // 추천 메뉴와 가게 조회
        List<HomeDashboardServiceResponse.RecommendedMenuInfo> recommendedMenus =
                dashboardRecommendationService.getRecommendedMenus(
                        memberId,
                        todayBudget,
                        recommendationLatitude,
                        recommendationLongitude,
                        DEFAULT_RECOMMENDATION_LIMIT
                );

        List<HomeDashboardServiceResponse.RecommendedStoreInfo> recommendedStores =
                dashboardRecommendationService.getRecommendedStores(
                        memberId,
                        recommendationLatitude,
                        recommendationLongitude,
                        DEFAULT_RECOMMENDATION_LIMIT
                );

        return HomeDashboardServiceResponse.builder()
                .location(new HomeDashboardServiceResponse.LocationInfo(
                        primaryAddress.getAddressHistoryId(),
                        primaryAddress.getAddress().getAlias(),
                        primaryAddress.getAddress().getFullAddress(),
                        primaryAddress.getAddress().getStreetNameAddress(),
                        primaryLatitude,
                        primaryLongitude,
                        primaryAddress.getIsPrimary()
                ))
                .budget(new HomeDashboardServiceResponse.BudgetInfo(
                        todayBudget.intValue(),
                        todaySpent.intValue(),
                        todayBudget.subtract(todaySpent).intValue(),
                        calculateUtilizationRate(todayBudget, todaySpent),
                        buildMealBudgetInfos(
                                mealBudgets,
                                breakfastSpent,
                                lunchSpent,
                                dinnerSpent,
                                otherSpent
                        )
                ))
                .recommendedMenus(recommendedMenus)
                .recommendedStores(recommendedStores)
                .build();
    }

    private List<HomeDashboardServiceResponse.MealBudgetInfo> buildMealBudgetInfos(
            List<MealBudget> mealBudgets,
            BigDecimal breakfastSpent,
            BigDecimal lunchSpent,
            BigDecimal dinnerSpent,
            BigDecimal otherSpent
    ) {
        return mealBudgets.stream()
                .map(mb -> {
                    Integer budget = mb.getMealBudget();
                    BigDecimal spent = switch (mb.getMealType()) {
                        case BREAKFAST -> breakfastSpent;
                        case LUNCH -> lunchSpent;
                        case DINNER -> dinnerSpent;
                        default -> otherSpent;
                    };

                    Integer mealRemaining = budget - spent.intValue();

                    return new HomeDashboardServiceResponse.MealBudgetInfo(
                            mb.getMealType().name(),
                            budget,
                            spent.intValue(),
                            mealRemaining
                    );
                })
                .toList();
    }

    private int calculateUtilizationRate(BigDecimal budget, BigDecimal spent) {
        if (budget.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return spent.multiply(BigDecimal.valueOf(100))
                .divide(budget, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }
}
