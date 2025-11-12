package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.api.home.service.dto.HomeDashboardServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.ResourceNotFoundException;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MealBudget;
import com.stdev.smartmealtable.domain.budget.MealBudgetRepository;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 홈 대시보드 조회 서비스 단위 테스트 (Mockist 스타일)
 * Repository를 Mock하여 orchestration 로직만 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HomeDashboardQueryService 단위 테스트")
class HomeDashboardQueryServiceTest {

    @InjectMocks
    private HomeDashboardQueryService homeDashboardQueryService;

    @Mock
    private AddressHistoryRepository addressHistoryRepository;

    @Mock
    private DailyBudgetRepository dailyBudgetRepository;

    @Mock
    private MealBudgetRepository mealBudgetRepository;

    @Mock
    private ExpenditureRepository expenditureRepository;

    @Mock
    private DashboardRecommendationService dashboardRecommendationService;

    @Test
    @DisplayName("홈 대시보드 조회 - 성공 (모든 데이터 존재)")
    void getHomeDashboard_Success_AllDataExists() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        // 주소 데이터
        Address address = Address.of(
                "우리집", null, "서울특별시 강남구 테헤란로 123",
                "101동 1234호", 37.497942, 127.027621, AddressType.HOME
        );
        AddressHistory primaryAddress = AddressHistory.reconstitute(
                1L, memberId, address, true, null
        );

        // 일일 예산 데이터 - reconstitute(budgetId, memberId, dailyFoodBudget, dailyUsedAmount, budgetDate)
        DailyBudget dailyBudget = DailyBudget.reconstitute(
                1L, memberId, 20000, 0, today
        );

        // 끼니별 예산 데이터 - reconstitute(mealBudgetId, dailyBudgetId, mealBudget, mealType, usedAmount, budgetDate)
        MealBudget breakfastBudget = MealBudget.reconstitute(
                Long.valueOf(1), Long.valueOf(1), Integer.valueOf(5000), MealType.BREAKFAST, Integer.valueOf(0), today
        );
        MealBudget lunchBudget = MealBudget.reconstitute(
                Long.valueOf(2), Long.valueOf(1), Integer.valueOf(7000), MealType.LUNCH, Integer.valueOf(0), today
        );
        MealBudget dinnerBudget = MealBudget.reconstitute(
                Long.valueOf(3), Long.valueOf(1), Integer.valueOf(8000), MealType.DINNER, Integer.valueOf(0), today
        );

        // 총 지출
        Long totalSpent = 15000L;

        // 끼니별 지출
        Map<MealType, Long> mealTypeSpent = Map.of(
                MealType.BREAKFAST, 4000L,
                MealType.LUNCH, 6000L,
                MealType.DINNER, 5000L
        );

        given(addressHistoryRepository.findPrimaryByMemberId(memberId))
                .willReturn(Optional.of(primaryAddress));
        given(dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(Optional.of(dailyBudget));
        given(mealBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(List.of(breakfastBudget, lunchBudget, dinnerBudget));
        given(expenditureRepository.getTotalAmountByPeriod(memberId, today, today))
                .willReturn(totalSpent);
        given(expenditureRepository.getAmountByMealTypeForPeriod(memberId, today, today))
                .willReturn(mealTypeSpent);
        given(dashboardRecommendationService.getRecommendedMenus(anyLong(), any(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());
        given(dashboardRecommendationService.getRecommendedStores(anyLong(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());

        // when
        HomeDashboardServiceResponse response = homeDashboardQueryService.getHomeDashboard(memberId);

        // then
        assertThat(response).isNotNull();
        
        // Location 검증
        assertThat(response.location()).isNotNull();
        assertThat(response.location().addressAlias()).isEqualTo("우리집");
        assertThat(response.location().roadAddress()).isEqualTo("서울특별시 강남구 테헤란로 123");
        assertThat(response.location().isPrimary()).isTrue();
        
        // Budget 검증
        assertThat(response.budget()).isNotNull();
        assertThat(response.budget().todayBudget()).isEqualTo(20000);
        assertThat(response.budget().todaySpent()).isEqualTo(15000);
        assertThat(response.budget().todayRemaining()).isEqualTo(5000);
        assertThat(response.budget().utilizationRate()).isEqualTo(75); // 15000/20000 * 100
        
        // MealBudget 검증
        assertThat(response.budget().mealBudgets()).hasSize(3);
        
        var breakfastInfo = response.budget().mealBudgets().stream()
                .filter(mb -> mb.mealType().equals("BREAKFAST"))
                .findFirst().orElseThrow();
        assertThat(breakfastInfo.budget()).isEqualTo(5000);
        assertThat(breakfastInfo.spent()).isEqualTo(4000);
        assertThat(breakfastInfo.remaining()).isEqualTo(1000);
        
        var lunchInfo = response.budget().mealBudgets().stream()
                .filter(mb -> mb.mealType().equals("LUNCH"))
                .findFirst().orElseThrow();
        assertThat(lunchInfo.budget()).isEqualTo(7000);
        assertThat(lunchInfo.spent()).isEqualTo(6000);
        assertThat(lunchInfo.remaining()).isEqualTo(1000);

        verify(addressHistoryRepository, times(1)).findPrimaryByMemberId(memberId);
        verify(dailyBudgetRepository, times(1)).findByMemberIdAndBudgetDate(memberId, today);
        verify(mealBudgetRepository, times(1)).findByMemberIdAndBudgetDate(memberId, today);
        verify(expenditureRepository, times(1)).getTotalAmountByPeriod(memberId, today, today);
        verify(expenditureRepository, times(1)).getAmountByMealTypeForPeriod(memberId, today, today);
    }

    @Test
    @DisplayName("홈 대시보드 조회 - 성공 (예산 미설정, 지출 없음)")
    void getHomeDashboard_Success_NoBudgetNoExpenditure() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        Address address = Address.of(
                "우리집", null, "서울특별시 강남구 테헤란로 123",
                "101동 1234호", 37.497942, 127.027621, AddressType.HOME
        );
        AddressHistory primaryAddress = AddressHistory.reconstitute(
                1L, memberId, address, true, null
        );

        given(addressHistoryRepository.findPrimaryByMemberId(memberId))
                .willReturn(Optional.of(primaryAddress));
        given(dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(Optional.empty());
        given(mealBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(List.of());
        given(expenditureRepository.getTotalAmountByPeriod(memberId, today, today))
                .willReturn(null);
        given(expenditureRepository.getAmountByMealTypeForPeriod(memberId, today, today))
                .willReturn(Map.of());
        given(dashboardRecommendationService.getRecommendedMenus(anyLong(), any(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());
        given(dashboardRecommendationService.getRecommendedStores(anyLong(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());

        // when
        HomeDashboardServiceResponse response = homeDashboardQueryService.getHomeDashboard(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.budget().todayBudget()).isEqualTo(0);
        assertThat(response.budget().todaySpent()).isEqualTo(0);
        assertThat(response.budget().todayRemaining()).isEqualTo(0);
        assertThat(response.budget().mealBudgets()).isEmpty();
    }

    @Test
    @DisplayName("홈 대시보드 조회 - 성공 (기타 식사 타입 지출 포함)")
    void getHomeDashboard_Success_WithOtherMealType() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        Address address = Address.of(
                "우리집", null, "서울특별시 강남구 테헤란로 123",
                "101동 1234호", 37.497942, 127.027621, AddressType.HOME
        );
        AddressHistory primaryAddress = AddressHistory.reconstitute(
                1L, memberId, address, true, null
        );

        DailyBudget dailyBudget = DailyBudget.reconstitute(
                1L, memberId, 20000, 0, today
        );

        MealBudget otherBudget = MealBudget.reconstitute(
                4L, 1L, 5000, MealType.OTHER, 0, today
        );

        Map<MealType, Long> mealTypeSpent = Map.of(
                MealType.BREAKFAST, 5000L,
                MealType.OTHER, 3000L
        );

        given(addressHistoryRepository.findPrimaryByMemberId(memberId))
                .willReturn(Optional.of(primaryAddress));
        given(dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(Optional.of(dailyBudget));
        given(mealBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(List.of(otherBudget));
        given(expenditureRepository.getTotalAmountByPeriod(memberId, today, today))
                .willReturn(8000L);
        given(expenditureRepository.getAmountByMealTypeForPeriod(memberId, today, today))
                .willReturn(mealTypeSpent);
        given(dashboardRecommendationService.getRecommendedMenus(anyLong(), any(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());
        given(dashboardRecommendationService.getRecommendedStores(anyLong(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());

        // when
        HomeDashboardServiceResponse response = homeDashboardQueryService.getHomeDashboard(memberId);

        // then
        var otherInfo = response.budget().mealBudgets().stream()
                .filter(mb -> mb.mealType().equals("OTHER"))
                .findFirst().orElseThrow();
        assertThat(otherInfo.spent()).isEqualTo(3000);
        assertThat(otherInfo.remaining()).isEqualTo(2000);
    }

    @Test
    @DisplayName("홈 대시보드 조회 - 실패 (기본 주소 없음)")
    void getHomeDashboard_Fail_NoPrimaryAddress() {
        // given
        Long memberId = 1L;

        given(addressHistoryRepository.findPrimaryByMemberId(memberId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> homeDashboardQueryService.getHomeDashboard(memberId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ADDRESS_NOT_FOUND);

        verify(addressHistoryRepository, times(1)).findPrimaryByMemberId(memberId);
        verify(dailyBudgetRepository, times(0)).findByMemberIdAndBudgetDate(any(), any());
    }

    @Test
    @DisplayName("홈 대시보드 조회 - 성공 (일부 끼니에만 지출 존재)")
    void getHomeDashboard_Success_PartialMealTypeExpenditure() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        Address address = Address.of(
                "회사", null, "서울특별시 강남구 삼성로 456",
                "B동 10층", 37.508123, 127.062345, AddressType.OFFICE
        );
        AddressHistory primaryAddress = AddressHistory.reconstitute(
                2L, memberId, address, true, null
        );

        DailyBudget dailyBudget = DailyBudget.reconstitute(
                1L, memberId, 30000, 0, today
        );

        MealBudget lunchBudget = MealBudget.reconstitute(
                2L, 1L, 15000, MealType.LUNCH, 0, today
        );

        // 점심과 저녁만 지출
        Map<MealType, Long> mealTypeSpent = Map.of(
                MealType.LUNCH, 10000L,
                MealType.DINNER, 15000L
        );

        given(addressHistoryRepository.findPrimaryByMemberId(memberId))
                .willReturn(Optional.of(primaryAddress));
        given(dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(Optional.of(dailyBudget));
        given(mealBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(List.of(lunchBudget));
        given(expenditureRepository.getTotalAmountByPeriod(memberId, today, today))
                .willReturn(25000L);
        given(expenditureRepository.getAmountByMealTypeForPeriod(memberId, today, today))
                .willReturn(mealTypeSpent);
        given(dashboardRecommendationService.getRecommendedMenus(anyLong(), any(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());
        given(dashboardRecommendationService.getRecommendedStores(anyLong(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());

        // when
        HomeDashboardServiceResponse response = homeDashboardQueryService.getHomeDashboard(memberId);

        // then
        assertThat(response.location().addressAlias()).isEqualTo("회사");
        assertThat(response.budget().todayBudget()).isEqualTo(30000);
        assertThat(response.budget().todaySpent()).isEqualTo(25000);
        assertThat(response.budget().todayRemaining()).isEqualTo(5000);
    }

    @Test
    @DisplayName("홈 대시보드 조회 - 경계값 테스트 (0원 예산)")
    void getHomeDashboard_EdgeCase_ZeroBudget() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        Address address = Address.of(
                "테스트주소", null, "서울특별시 종로구",
                "1층", 37.5, 127.0, AddressType.ETC
        );
        AddressHistory primaryAddress = AddressHistory.reconstitute(
                1L, memberId, address, true, null
        );

        DailyBudget dailyBudget = DailyBudget.reconstitute(
                1L, memberId, 0, 0, today
        );

        given(addressHistoryRepository.findPrimaryByMemberId(memberId))
                .willReturn(Optional.of(primaryAddress));
        given(dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(Optional.of(dailyBudget));
        given(mealBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(List.of());
        given(expenditureRepository.getTotalAmountByPeriod(memberId, today, today))
                .willReturn(0L);
        given(expenditureRepository.getAmountByMealTypeForPeriod(memberId, today, today))
                .willReturn(Map.of());
        given(dashboardRecommendationService.getRecommendedMenus(anyLong(), any(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());
        given(dashboardRecommendationService.getRecommendedStores(anyLong(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());

        // when
        HomeDashboardServiceResponse response = homeDashboardQueryService.getHomeDashboard(memberId);

        // then
        assertThat(response.budget().todayBudget()).isEqualTo(0);
        assertThat(response.budget().todaySpent()).isEqualTo(0);
        assertThat(response.budget().utilizationRate()).isEqualTo(0);
    }

    @Test
    @DisplayName("홈 대시보드 조회 - 경계값 테스트 (매우 큰 금액)")
    void getHomeDashboard_EdgeCase_LargeAmount() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        Address address = Address.of(
                "우리집", null, "서울특별시 강남구",
                "101동", 37.5, 127.0, AddressType.HOME
        );
        AddressHistory primaryAddress = AddressHistory.reconstitute(
                1L, memberId, address, true, null
        );

        int largeBudget = 999999999; // 약 10억
        long largeSpent = 888888888L;

        DailyBudget dailyBudget = DailyBudget.reconstitute(
                1L, memberId, largeBudget, 0, today
        );

        MealBudget breakfastBudget = MealBudget.reconstitute(
                1L, 1L, 100000000, MealType.BREAKFAST, 0, today
        );

        Map<MealType, Long> mealTypeSpent = Map.of(
                MealType.BREAKFAST, 100000000L,
                MealType.LUNCH, 300000000L,
                MealType.DINNER, 400000000L,
                MealType.OTHER, 88888888L
        );

        given(addressHistoryRepository.findPrimaryByMemberId(memberId))
                .willReturn(Optional.of(primaryAddress));
        given(dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(Optional.of(dailyBudget));
        given(mealBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(List.of(breakfastBudget));
        given(expenditureRepository.getTotalAmountByPeriod(memberId, today, today))
                .willReturn(largeSpent);
        given(expenditureRepository.getAmountByMealTypeForPeriod(memberId, today, today))
                .willReturn(mealTypeSpent);
        given(dashboardRecommendationService.getRecommendedMenus(anyLong(), any(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());
        given(dashboardRecommendationService.getRecommendedStores(anyLong(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());

        // when
        HomeDashboardServiceResponse response = homeDashboardQueryService.getHomeDashboard(memberId);

        // then
        assertThat(response.budget().todayBudget()).isEqualTo(largeBudget);
        assertThat(response.budget().todaySpent()).isEqualTo((int) largeSpent);

        var breakfastInfo = response.budget().mealBudgets().stream()
                .filter(mb -> mb.mealType().equals("BREAKFAST"))
                .findFirst().orElseThrow();
        assertThat(breakfastInfo.spent()).isEqualTo(100000000);
    }

    @Test
    @DisplayName("홈 대시보드 조회 - 성공 (예산 초과 시나리오)")
    void getHomeDashboard_Success_BudgetExceeded() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        Address address = Address.of(
                "우리집", null, "서울특별시 강남구",
                "101동", 37.5, 127.0, AddressType.HOME
        );
        AddressHistory primaryAddress = AddressHistory.reconstitute(
                1L, memberId, address, true, null
        );

        DailyBudget dailyBudget = DailyBudget.reconstitute(
                1L, memberId, 10000, 0, today
        );

        given(addressHistoryRepository.findPrimaryByMemberId(memberId))
                .willReturn(Optional.of(primaryAddress));
        given(dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(Optional.of(dailyBudget));
        given(mealBudgetRepository.findByMemberIdAndBudgetDate(memberId, today))
                .willReturn(List.of());
        given(expenditureRepository.getTotalAmountByPeriod(memberId, today, today))
                .willReturn(15000L); // 예산 초과
        given(expenditureRepository.getAmountByMealTypeForPeriod(memberId, today, today))
                .willReturn(Map.of());
        given(dashboardRecommendationService.getRecommendedMenus(anyLong(), any(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());
        given(dashboardRecommendationService.getRecommendedStores(anyLong(), nullable(Double.class), nullable(Double.class), anyInt()))
                .willReturn(List.of());

        // when
        HomeDashboardServiceResponse response = homeDashboardQueryService.getHomeDashboard(memberId);

        // then
        assertThat(response.budget().todayBudget()).isEqualTo(10000);
        assertThat(response.budget().todaySpent()).isEqualTo(15000);
        assertThat(response.budget().todayRemaining()).isEqualTo(-5000); // 음수
        assertThat(response.budget().utilizationRate()).isEqualTo(150); // 150%
    }
}
