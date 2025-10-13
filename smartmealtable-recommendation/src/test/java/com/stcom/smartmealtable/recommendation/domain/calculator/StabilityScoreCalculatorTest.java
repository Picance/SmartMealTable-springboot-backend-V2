package com.stcom.smartmealtable.recommendation.domain.calculator;

import com.stcom.smartmealtable.recommendation.domain.model.CalculationContext;
import com.stcom.smartmealtable.recommendation.domain.model.ExpenditureRecord;
import com.stcom.smartmealtable.recommendation.domain.model.UserProfile;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 안정성 점수 계산기 테스트
 */
@DisplayName("안정성 점수 계산기 테스트")
class StabilityScoreCalculatorTest {

    private StabilityScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new StabilityScoreCalculator();
    }

    @Test
    @DisplayName("선호 카테고리 점수 계산 - 좋아요(100) → 높은 점수")
    void calculatePreferenceScore_Liked() {
        // given
        Store store = createStore(1L, "한식집", 1L, 50, 8000);
        
        Map<Long, Integer> preferences = new HashMap<>();
        preferences.put(1L, 100); // 카테고리 1번 좋아요
        
        UserProfile userProfile = createUserProfile(preferences, new HashMap<>(), new HashMap<>());
        CalculationContext context = createContext(List.of(store), userProfile);

        // when
        double score = calculator.calculate(store, userProfile, context);

        // then
        // 선호도 40% 가중치, 리뷰 20% 가중치만 반영 (지출 내역 없음)
        assertThat(score).isGreaterThan(30.0); // 최소 40점 (선호도 100 * 0.4)
    }

    @Test
    @DisplayName("선호 카테고리 점수 계산 - 싫어요(-100) → 낮은 점수")
    void calculatePreferenceScore_Disliked() {
        // given
        Store store = createStore(1L, "한식집", 1L, 50, 8000);
        
        Map<Long, Integer> preferences = new HashMap<>();
        preferences.put(1L, -100); // 카테고리 1번 싫어요
        
        UserProfile userProfile = createUserProfile(preferences, new HashMap<>(), new HashMap<>());
        CalculationContext context = createContext(List.of(store), userProfile);

        // when
        double score = calculator.calculate(store, userProfile, context);

        // then
        // 싫어요는 0점, 선호도 가중치 40%가 0이 됨
        assertThat(score).isLessThan(30.0);
    }

    @Test
    @DisplayName("신규 사용자 (지출 내역 3건 미만) - 지출 점수 0 처리")
    void calculateExpenditureScore_NewUser() {
        // given
        Store store = createStore(1L, "한식집", 1L, 50, 8000);
        
        Map<Long, Integer> preferences = new HashMap<>();
        preferences.put(1L, 50); // 중립
        
        Map<LocalDate, ExpenditureRecord> expenditures = new HashMap<>();
        // 2건만 추가 (3건 미만)
        expenditures.put(LocalDate.now().minusDays(10), createExpenditure(1L, 1L, 5000));
        expenditures.put(LocalDate.now().minusDays(20), createExpenditure(1L, 1L, 6000));
        
        UserProfile userProfile = createUserProfile(preferences, expenditures, new HashMap<>());
        CalculationContext context = createContext(List.of(store), userProfile);

        // when
        double score = calculator.calculate(store, userProfile, context);

        // then
        // 지출 가중치(40%)가 0이므로, 선호도(40%) + 리뷰(20%)만 반영
        assertThat(score).isLessThan(70.0);
    }

    @Test
    @DisplayName("과거 지출 기록 - 시간 감쇠 적용")
    void calculateExpenditureScore_WithTimeDecay() {
        // given
        Store store = createStore(1L, "한식집", 1L, 100, 8000);
        
        Map<Long, Integer> preferences = new HashMap<>();
        preferences.put(1L, 100);
        
        Map<LocalDate, ExpenditureRecord> expenditures = new HashMap<>();
        // 최근 지출 (가중치 높음)
        expenditures.put(LocalDate.now().minusDays(5), createExpenditure(1L, 1L, 10000));
        // 오래된 지출 (가중치 낮음)
        expenditures.put(LocalDate.now().minusDays(100), createExpenditure(1L, 1L, 10000));
        expenditures.put(LocalDate.now().minusDays(150), createExpenditure(2L, 2L, 10000));
        
        UserProfile userProfile = createUserProfile(preferences, expenditures, new HashMap<>());
        CalculationContext context = createContext(List.of(store), userProfile);

        // when
        double score = calculator.calculate(store, userProfile, context);

        // then
        assertThat(score).isGreaterThan(50.0); // 최근 지출이 있어 높은 점수
    }

    @Test
    @DisplayName("리뷰 신뢰도 - 리뷰 많은 가게가 높은 점수")
    void calculateReviewScore_HighReviews() {
        // given
        Store highReviewStore = createStore(1L, "인기 맛집", 1L, 500, 8000);
        Store lowReviewStore = createStore(2L, "신규 가게", 1L, 10, 8000);
        
        Map<Long, Integer> preferences = new HashMap<>();
        preferences.put(1L, 0); // 중립
        
        UserProfile userProfile = createUserProfile(preferences, new HashMap<>(), new HashMap<>());
        CalculationContext context = createContext(List.of(highReviewStore, lowReviewStore), userProfile);

        // when
        double scoreHigh = calculator.calculate(highReviewStore, userProfile, context);
        double scoreLow = calculator.calculate(lowReviewStore, userProfile, context);

        // then
        assertThat(scoreHigh).isGreaterThan(scoreLow);
    }

    // ==================== Helper Methods ====================

    private Store createStore(Long id, String name, Long categoryId, Integer reviewCount, Integer avgPrice) {
        return Store.builder()
                .storeId(id)
                .name(name)
                .categoryId(categoryId)
                .reviewCount(reviewCount)
                .averagePrice(avgPrice)
                .latitude(BigDecimal.valueOf(37.5665))
                .longitude(BigDecimal.valueOf(126.9780))
                .viewCount(0)
                .favoriteCount(0)
                .storeType(StoreType.RESTAURANT)
                .registeredAt(LocalDateTime.now().minusDays(30))
                .build();
    }

    private UserProfile createUserProfile(
            Map<Long, Integer> preferences,
            Map<LocalDate, ExpenditureRecord> expenditures,
            Map<Long, LocalDate> lastVisits
    ) {
        return UserProfile.builder()
                .memberId(1L)
                .recommendationType(RecommendationType.BALANCED)
                .currentLatitude(BigDecimal.valueOf(37.5665))
                .currentLongitude(BigDecimal.valueOf(126.9780))
                .categoryPreferences(preferences)
                .recentExpenditures(expenditures)
                .storeLastVisitDates(lastVisits)
                .build();
    }

    private ExpenditureRecord createExpenditure(Long categoryId, Long storeId, Integer amount) {
        return ExpenditureRecord.builder()
                .categoryId(categoryId)
                .storeId(storeId)
                .amount(amount)
                .expendedAt(LocalDate.now())
                .build();
    }

    private CalculationContext createContext(List<Store> stores, UserProfile userProfile) {
        return CalculationContext.from(stores, userProfile);
    }
}
