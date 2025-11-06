package com.stdev.smartmealtable.api.recommendation.dto;

import com.stcom.smartmealtable.recommendation.domain.model.RecommendationResult;
import com.stdev.smartmealtable.core.pagination.CursorIdentifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 추천 결과 응답 DTO
 *
 * <p>커서 기반 페이징을 지원하기 위해 CursorIdentifiable을 구현합니다.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponseDto implements CursorIdentifiable {

    /**
     * 가게 ID
     */
    private Long storeId;

    /**
     * 가게명
     */
    private String storeName;

    /**
     * 카테고리 ID
     */
    private Long categoryId;

    /**
     * 추천 점수 (0~100)
     */
    private Double score;

    /**
     * 거리 (km)
     */
    private Double distance;

    /**
     * 평균 가격
     */
    private Integer averagePrice;

    /**
     * 리뷰 수
     */
    private Integer reviewCount;

    /**
     * 가게 이미지 URL
     */
    private String imageUrl;

    /**
     * 위도
     */
    private BigDecimal latitude;

    /**
     * 경도
     */
    private BigDecimal longitude;

    /**
     * 도메인 모델로부터 DTO 생성
     */
    public static RecommendationResponseDto from(RecommendationResult result) {
        return RecommendationResponseDto.builder()
                .storeId(result.getStoreId())
                .storeName(result.getStoreName())
                .categoryId(result.getCategoryId())
                .score(result.getFinalScore())
                .distance(result.getDistance())
                .averagePrice(result.getAveragePrice())
                .reviewCount(result.getReviewCount())
                .imageUrl(result.getImageUrl())
                .latitude(result.getLatitude())
                .longitude(result.getLongitude())
                .build();
    }

    /**
     * 도메인 모델 리스트로부터 DTO 리스트 생성
     */
    public static List<RecommendationResponseDto> fromList(List<RecommendationResult> results) {
        return results.stream()
                .map(RecommendationResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 커서 페이징을 위한 ID 반환
     *
     * @return 가게 ID
     */
    @Override
    public Long getCursorId() {
        return storeId;
    }
}
