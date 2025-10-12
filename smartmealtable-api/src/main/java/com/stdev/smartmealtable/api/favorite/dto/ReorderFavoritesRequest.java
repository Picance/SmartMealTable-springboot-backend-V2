package com.stdev.smartmealtable.api.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 즐겨찾기 순서 변경 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderFavoritesRequest {
    
    /**
     * 순서 변경 정보 목록
     */
    private List<FavoriteOrderDto> favoriteOrders;
    
    /**
     * 개별 순서 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteOrderDto {
        /**
         * 즐겨찾기 ID
         */
        private Long favoriteId;
        
        /**
         * 새로운 순서 (우선순위)
         */
        private Long priority;
    }
}
