package com.stdev.smartmealtable.api.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 즐겨찾기 순서 변경 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderFavoritesResponse {
    
    /**
     * 변경된 즐겨찾기 개수
     */
    private Integer updatedCount;
    
    /**
     * 성공 메시지
     */
    private String message;
}
