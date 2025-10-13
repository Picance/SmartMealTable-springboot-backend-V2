package com.stcom.smartmealtable.recommendation.domain.model;

import com.stdev.smartmealtable.domain.store.Store;
import lombok.Builder;
import lombok.Getter;

import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * 점수 계산 컨텍스트
 * 
 * <p>정규화를 위한 min/max 값과 통계 정보를 포함합니다.</p>
 */
@Getter
@Builder
public class CalculationContext {
    
    // ==================== Review 관련 ====================
    
    /**
     * 최소 리뷰 수
     */
    private final double minReviews;
    
    /**
     * 최대 리뷰 수
     */
    private final double maxReviews;
    
    // ==================== 조회수 관련 ====================
    
    /**
     * 최소 조회수 (최근 7일)
     */
    private final long minViews7Days;
    
    /**
     * 최대 조회수 (최근 7일)
     */
    private final long maxViews7Days;
    
    // ==================== 가성비 관련 ====================
    
    /**
     * 최소 가성비 값
     */
    private final double minValueForMoney;
    
    /**
     * 최대 가성비 값
     */
    private final double maxValueForMoney;
    
    // ==================== 거리 관련 ====================
    
    /**
     * 최소 거리 (km)
     */
    private final double minDistance;
    
    /**
     * 최대 거리 (km)
     */
    private final double maxDistance;
    
    // ==================== 참조용 데이터 ====================
    
    /**
     * 전체 가게 리스트
     */
    private final List<Store> allStores;
    
    /**
     * 필터링된 가게 리스트로부터 Context 생성
     * 
     * @param stores 필터링된 가게 리스트
     * @param userProfile 사용자 프로필
     * @return 계산 컨텍스트
     */
    public static CalculationContext from(List<Store> stores, UserProfile userProfile) {
        // Review 통계
        DoubleSummaryStatistics reviewStats = stores.stream()
                .mapToDouble(store -> store.getReviewCount() != null ? store.getReviewCount() : 0)
                .summaryStatistics();
        
        // 조회수 통계 (임시로 viewCount 사용, 추후 viewCountLast7Days로 변경 필요)
        DoubleSummaryStatistics viewStats = stores.stream()
                .mapToDouble(store -> store.getViewCount() != null ? store.getViewCount() : 0)
                .summaryStatistics();
        
        // 가성비 통계 (log(1 + reviews) / avg_price)
        DoubleSummaryStatistics valueStats = stores.stream()
                .filter(store -> store.getAveragePrice() != null && store.getAveragePrice() > 0)
                .mapToDouble(store -> Math.log(1 + (store.getReviewCount() != null ? store.getReviewCount() : 0)) 
                                      / store.getAveragePrice())
                .summaryStatistics();
        
        // 거리 통계 (Haversine 거리 계산)
        DoubleSummaryStatistics distanceStats = stores.stream()
                .mapToDouble(store -> calculateDistance(
                        userProfile.getCurrentLatitude().doubleValue(),
                        userProfile.getCurrentLongitude().doubleValue(),
                        store.getLatitude().doubleValue(),
                        store.getLongitude().doubleValue()
                ))
                .summaryStatistics();
        
        return CalculationContext.builder()
                .minReviews(reviewStats.getMin())
                .maxReviews(reviewStats.getMax())
                .minViews7Days((long) viewStats.getMin())
                .maxViews7Days((long) viewStats.getMax())
                .minValueForMoney(valueStats.getMin())
                .maxValueForMoney(valueStats.getMax())
                .minDistance(distanceStats.getMin())
                .maxDistance(distanceStats.getMax())
                .allStores(stores)
                .build();
    }
    
    /**
     * Haversine 공식을 사용한 두 좌표 간 거리 계산 (km 단위)
     */
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // km
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
}
