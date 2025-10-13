package com.stdev.smartmealtable.api.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 추천 목록 조회 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "추천 목록 조회 요청")
public class RecommendationRequestDto {

    @Schema(description = "현재 위도", example = "37.5665")
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
    private BigDecimal latitude;

    @Schema(description = "현재 경도", example = "126.9780")
    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
    private BigDecimal longitude;

    @Schema(description = "검색 반경 (km)", example = "1.0", defaultValue = "0.5")
    @DecimalMin(value = "0.1", message = "반경은 0.1km 이상이어야 합니다")
    @DecimalMax(value = "10.0", message = "반경은 10km 이하여야 합니다")
    @Builder.Default
    private Double radius = 0.5;

    @Schema(description = "정렬 기준", example = "SCORE", defaultValue = "SCORE")
    @Builder.Default
    private SortBy sortBy = SortBy.SCORE;

    @Schema(description = "불호 음식 포함 여부", example = "false", defaultValue = "false")
    @Builder.Default
    private Boolean includeDisliked = false;

    @Schema(description = "영업 중인 가게만 조회", example = "false", defaultValue = "false")
    @Builder.Default
    private Boolean openNow = false;

    @Schema(description = "가게 타입", example = "ALL", defaultValue = "ALL")
    @Builder.Default
    private StoreTypeFilter storeType = StoreTypeFilter.ALL;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    @Builder.Default
    private Integer size = 20;

    /**
     * 정렬 기준 Enum
     */
    public enum SortBy {
        SCORE,              // 추천 점수
        DISTANCE,           // 거리
        REVIEW,             // 리뷰 수
        PRICE_LOW,          // 가격 낮은 순
        PRICE_HIGH,         // 가격 높은 순
        FAVORITE,           // 즐겨찾기 많은 순
        INTEREST_HIGH,      // 관심도 높은 순
        INTEREST_LOW        // 관심도 낮은 순
    }

    /**
     * 가게 타입 필터
     */
    public enum StoreTypeFilter {
        ALL,                // 전체
        CAMPUS_RESTAURANT,  // 학생식당
        RESTAURANT          // 일반음식점
    }
}
