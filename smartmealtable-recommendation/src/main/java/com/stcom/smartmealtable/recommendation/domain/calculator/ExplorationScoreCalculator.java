package com.stcom.smartmealtable.recommendation.domain.calculator;

import com.stcom.smartmealtable.recommendation.domain.model.CalculationContext;
import com.stcom.smartmealtable.recommendation.domain.model.ExpenditureRecord;
import com.stcom.smartmealtable.recommendation.domain.model.UserProfile;
import com.stcom.smartmealtable.recommendation.util.NormalizationUtil;
import com.stdev.smartmealtable.domain.store.Store;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * 탐험성 점수 계산기
 * 
 * <p>사용자에게 새로운 경험과 다양성을 제공하기 위한 점수를 계산합니다.</p>
 * 
 * <ul>
 *   <li>카테고리 신선도 (40%): 최근 방문 빈도가 낮은 카테고리</li>
 *   <li>음식점 신규성 (30%): 방문 이력이 없거나 오래된 가게</li>
 *   <li>최근 관심도 (30%): 최근 인기있는 가게</li>
 * </ul>
 */
public class ExplorationScoreCalculator implements ScoreCalculator {

    private static final double CATEGORY_FRESHNESS_WEIGHT = 0.4;  // 40%
    private static final double STORE_NEWNESS_WEIGHT = 0.3;       // 30%
    private static final double RECENT_INTEREST_WEIGHT = 0.3;     // 30%

    @Override
    public double calculate(Store store, UserProfile userProfile, CalculationContext context) {
        double freshnessScore = calculateCategoryFreshnessScore(store, userProfile);
        double newnessScore = calculateStoreNewnessScore(store, userProfile);
        double interestScore = calculateRecentInterestScore(store, context);

        return (freshnessScore * CATEGORY_FRESHNESS_WEIGHT) +
               (newnessScore * STORE_NEWNESS_WEIGHT) +
               (interestScore * RECENT_INTEREST_WEIGHT);
    }

    /**
     * 카테고리 신선도 점수 계산
     * 
     * <p>최근 30일간 해당 카테고리 방문 비중의 역수를 계산합니다.</p>
     * <p>자주 안 간 카테고리일수록 높은 점수를 부여합니다.</p>
     * <p>가게가 여러 카테고리를 가진 경우, 주 카테고리(첫 번째)를 사용합니다.</p>
     * 
     * @param store 가게
     * @param userProfile 사용자 프로필
     * @return 0~100 점수
     */
    private double calculateCategoryFreshnessScore(Store store, UserProfile userProfile) {
        Map<LocalDate, ExpenditureRecord> recentExpenditures = userProfile.getRecentExpenditures(30);

        // 신규 사용자 처리 (모든 카테고리 동일 점수)
        if (recentExpenditures.isEmpty()) {
            return 50.0;
        }

        // 주 카테고리 ID (첫 번째 카테고리 사용)
        Long primaryCategoryId = store.getCategoryIds() != null && !store.getCategoryIds().isEmpty() 
                ? store.getCategoryIds().get(0) 
                : null;

        if (primaryCategoryId == null) {
            return 50.0; // 카테고리가 없으면 중간 점수
        }

        // 해당 카테고리 방문 횟수
        long categoryCount = recentExpenditures.values().stream()
                .filter(exp -> primaryCategoryId.equals(exp.getCategoryId()))
                .count();

        // 전체 지출 대비 비중
        double categoryProportion = (double) categoryCount / recentExpenditures.size();

        // 역수 계산 (안 간 카테고리일수록 높은 점수)
        return (1 - categoryProportion) * 100;
    }

    /**
     * 음식점 신규성 점수 계산
     * 
     * <p>마지막 방문 후 경과일 (60%) + 가게 신규성 (40%)</p>
     * 
     * @param store 가게
     * @param userProfile 사용자 프로필
     * @return 0~100 점수
     */
    private double calculateStoreNewnessScore(Store store, UserProfile userProfile) {
        // 1) 마지막 방문 후 경과일
        LocalDate lastVisit = userProfile.getLastVisitDate(store.getStoreId());
        double visitScore;
        if (lastVisit == null) {
            visitScore = 100.0; // 한 번도 안 간 가게
        } else {
            long daysAgo = ChronoUnit.DAYS.between(lastVisit, LocalDate.now());
            visitScore = Math.min(daysAgo / 180.0 * 100, 100); // 180일 이상이면 100점
        }

        // 2) 가게 등록일 (신규 가게일수록 높은 점수)
        LocalDateTime registeredAt = store.getRegisteredAt();
        double registeredScore;
        if (registeredAt == null) {
            registeredScore = 50.0; // 정보 없음
        } else {
            long daysSinceRegistered = ChronoUnit.DAYS.between(registeredAt.toLocalDate(), LocalDate.now());
            registeredScore = Math.max(100 - (daysSinceRegistered / 30.0 * 10), 0); // 30일 = 100점, 300일+ = 0점
        }

        return visitScore * 0.6 + registeredScore * 0.4;
    }

    /**
     * 최근 관심도 점수 계산
     * 
     * <p>최근 조회수를 로그 정규화하여 인기있는 가게에 높은 점수를 부여합니다.</p>
     * 
     * @param store 가게
     * @param context 계산 컨텍스트
     * @return 0~100 점수
     */
    private double calculateRecentInterestScore(Store store, CalculationContext context) {
        // 임시로 viewCount 사용 (추후 viewCountLast7Days로 변경 필요)
        long views = store.getViewCount() != null ? store.getViewCount() : 0;
        
        // 로그 정규화로 큰 값의 영향력 완화
        return NormalizationUtil.normalizeLog(views, context.getMinViews7Days(), context.getMaxViews7Days());
    }
}
