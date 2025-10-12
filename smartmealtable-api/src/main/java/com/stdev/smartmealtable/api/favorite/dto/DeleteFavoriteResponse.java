package com.stdev.smartmealtable.api.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 즐겨찾기 삭제 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteFavoriteResponse {
    
    /**
     * 삭제된 즐겨찾기 ID
     */
    private Long favoriteId;
    
    /**
     * 삭제 성공 메시지
     */
    private String message;
}
