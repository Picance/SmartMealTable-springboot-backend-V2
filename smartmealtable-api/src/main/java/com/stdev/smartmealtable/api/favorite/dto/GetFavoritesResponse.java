package com.stdev.smartmealtable.api.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 즐겨찾기 목록 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetFavoritesResponse {
    
    /**
     * 즐겨찾기 목록
     */
    private List<FavoriteStoreDto> favorites;
    
    /**
     * 총 즐겨찾기 개수
     */
    private Integer totalCount;
}
