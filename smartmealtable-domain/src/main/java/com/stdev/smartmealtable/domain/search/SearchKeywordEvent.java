package com.stdev.smartmealtable.domain.search;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 자동완성 검색 이벤트 도메인 엔티티
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchKeywordEvent {

    /**
     * 이벤트 고유 ID
     */
    private Long searchKeywordEventId;

    /**
     * 검색한 회원 ID (비로그인 시 null)
     */
    private Long memberId;

    /**
     * 사용자가 입력한 원본 키워드
     */
    private String rawKeyword;

    /**
     * 정규화된 검색 키워드
     */
    private String normalizedKeyword;

    /**
     * 검색 결과 중 선택된 음식 ID (검색 단계에서는 null)
     */
    private Long clickedFoodId;

    /**
     * 검색 시점의 위도
     */
    private BigDecimal latitude;

    /**
     * 검색 시점의 경도
     */
    private BigDecimal longitude;

    /**
     * 이벤트 생성 시각
     */
    private LocalDateTime createdAt;

    public static SearchKeywordEvent of(
            Long memberId,
            String rawKeyword,
            String normalizedKeyword,
            Long clickedFoodId,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        return SearchKeywordEvent.builder()
                .memberId(memberId)
                .rawKeyword(rawKeyword)
                .normalizedKeyword(normalizedKeyword)
                .clickedFoodId(clickedFoodId)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
