package com.stdev.smartmealtable.api.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 목록 조회 응답 DTO (개별 아이템)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStoreDto {
    
    /**
     * 즐겨찾기 ID
     */
    private Long favoriteId;
    
    /**
     * 가게 ID
     */
    private Long storeId;
    
    /**
     * 가게명
     */
    private String storeName;
    
    /**
     * 카테고리명
     */
    private String categoryName;
    
    /**
     * 리뷰 수
     */
    private Integer reviewCount;
    
    /**
     * 평균 가격
     */
    private Integer averagePrice;
    
    /**
     * 주소
     */
    private String address;
    
    /**
     * 대표 이미지 URL
     */
    private String imageUrl;
    
    /**
     * 표시 순서
     */
    private Long priority;
    
    /**
     * 즐겨찾기 등록 시각
     */
    private LocalDateTime favoritedAt;
}
