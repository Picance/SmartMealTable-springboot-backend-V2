package com.stdev.smartmealtable.api.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 추가 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddFavoriteResponse {
    
    /**
     * 생성된 즐겨찾기 ID
     */
    private Long favoriteId;
    
    /**
     * 가게 ID
     */
    private Long storeId;
    
    /**
     * 표시 순서
     */
    private Long priority;
    
    /**
     * 즐겨찾기 등록 시각
     */
    private LocalDateTime favoritedAt;
}
