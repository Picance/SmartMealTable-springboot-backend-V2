package com.stdev.smartmealtable.api.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stcom.smartmealtable.recommendation.domain.model.RecommendationResult;
import com.stcom.smartmealtable.recommendation.domain.model.ScoreDetail;
import com.stdev.smartmealtable.core.pagination.CursorIdentifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    @JsonProperty("name")
    private String storeName;

    /**
     * 카테고리 ID
     */
    private Long categoryId;

    /**
     * 카테고리 이름
     */
    private String categoryName;

    /**
     * 주소
     */
    private String address;

    /**
     * 추천 점수 (0~100)
     */
    @JsonProperty("recommendationScore")
    private Double score;

    /**
     * 추천 점수 상세
     */
    private Scores scores;

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
     * 즐겨찾기 여부
     */
    private Boolean isFavorite;

    /**
     * 영업 여부
     */
    private Boolean isOpen;

    /**
     * 도메인 모델로부터 DTO 생성
     */
    public static RecommendationResponseDto from(
            RecommendationResult result,
            String categoryName,
            boolean isFavorite,
            boolean isOpen
    ) {
        return RecommendationResponseDto.builder()
                .storeId(result.getStoreId())
                .storeName(result.getStoreName())
                .categoryId(result.getCategoryId())
                .categoryName(categoryName)
                .address(result.getAddress())
                .score(result.getFinalScore())
                .scores(Scores.from(result.getScoreDetail()))
                .distance(result.getDistance())
                .averagePrice(result.getAveragePrice())
                .reviewCount(result.getReviewCount())
                .imageUrl(result.getImageUrl())
                .latitude(result.getLatitude())
                .longitude(result.getLongitude())
                .isFavorite(isFavorite)
                .isOpen(isOpen)
                .build();
    }

    /**
     * 도메인 모델 리스트로부터 DTO 리스트 생성
     */
    public static List<RecommendationResponseDto> fromList(
            List<RecommendationResult> results,
            Map<Long, String> categoryNameMap,
            Set<Long> favoriteStoreIds,
            Map<Long, Boolean> operationStatusMap
    ) {
        return results.stream()
                .map(result -> RecommendationResponseDto.from(
                        result,
                        categoryNameMap.getOrDefault(result.getCategoryId(), null),
                        favoriteStoreIds.contains(result.getStoreId()),
                        operationStatusMap.getOrDefault(result.getStoreId(), Boolean.FALSE)
                ))
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

    /**
     * 점수 요약 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Scores {
        private Double stability;
        private Double exploration;
        private Double budgetEfficiency;
        private Double accessibility;

        public static Scores from(ScoreDetail detail) {
            if (detail == null) {
                return null;
            }
            return Scores.builder()
                    .stability(detail.getStabilityScore())
                    .exploration(detail.getExplorationScore())
                    .budgetEfficiency(detail.getBudgetEfficiencyScore())
                    .accessibility(detail.getAccessibilityScore())
                    .build();
        }
    }
}
