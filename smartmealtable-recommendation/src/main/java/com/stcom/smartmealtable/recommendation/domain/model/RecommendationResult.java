package com.stcom.smartmealtable.recommendation.domain.model;

import com.stdev.smartmealtable.domain.store.Store;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 추천 결과
 * 
 * <p>개별 가게에 대한 추천 점수와 상세 정보를 포함합니다.</p>
 */
@Getter
@Builder
public class RecommendationResult {
    
    /**
     * 가게 ID
     */
    private final Long storeId;
    
    /**
     * 가게명
     */
    private final String storeName;
    
    /**
     * 카테고리 ID
     */
    private final Long categoryId;
    
    /**
     * 최종 추천 점수 (0~100)
     */
    private final double finalScore;
    
    /**
     * 거리 (km)
     */
    private final double distance;
    
    /**
     * 평균 가격
     */
    private final Integer averagePrice;
    
    /**
     * 리뷰 수
     */
    private final Integer reviewCount;
    
    /**
     * 가게 이미지 URL
     */
    private final String imageUrl;
    
    /**
     * 위도
     */
    private final BigDecimal latitude;
    
    /**
     * 경도
     */
    private final BigDecimal longitude;
    
    /**
     * 점수 상세 정보 (선택적)
     */
    private final ScoreDetail scoreDetail;
    
    /**
     * Store 도메인으로부터 RecommendationResult 생성
     */
    public static RecommendationResult from(
            Store store,
            double finalScore,
            double distance,
            ScoreDetail scoreDetail
    ) {
        return RecommendationResult.builder()
                .storeId(store.getStoreId())
                .storeName(store.getName())
                .categoryId(store.getCategoryId())
                .finalScore(finalScore)
                .distance(distance)
                .averagePrice(store.getAveragePrice())
                .reviewCount(store.getReviewCount())
                .imageUrl(store.getImageUrl())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .scoreDetail(scoreDetail)
                .build();
    }
}
