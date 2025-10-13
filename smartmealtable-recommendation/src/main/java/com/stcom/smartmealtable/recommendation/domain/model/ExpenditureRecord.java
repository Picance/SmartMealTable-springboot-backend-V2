package com.stcom.smartmealtable.recommendation.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 지출 레코드
 * 추천 시스템에서 사용하는 간소화된 지출 정보
 */
@Getter
@Builder
public class ExpenditureRecord {
    
    /**
     * 지출 날짜
     */
    private final LocalDate expendedAt;
    
    /**
     * 지출 금액
     */
    private final Integer amount;
    
    /**
     * 카테고리 ID
     */
    private final Long categoryId;
    
    /**
     * 가게 ID (있는 경우)
     */
    private final Long storeId;
}
