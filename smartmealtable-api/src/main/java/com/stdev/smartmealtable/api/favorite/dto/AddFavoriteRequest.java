package com.stdev.smartmealtable.api.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 즐겨찾기 추가 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddFavoriteRequest {
    
    /**
     * 즐겨찾기할 가게 ID
     */
    private Long storeId;
}
