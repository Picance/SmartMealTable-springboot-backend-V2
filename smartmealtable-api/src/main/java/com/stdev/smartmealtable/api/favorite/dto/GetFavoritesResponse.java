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
     * 필터 조건을 만족하는 총 즐겨찾기 개수 (페이징 적용 전)
     */
    private Integer totalCount;

    /**
     * 현재 조건을 만족하는 즐겨찾기 중 영업 중인 가게 수
     */
    private Integer openCount;

    /**
     * 클라이언트가 요청한 페이징 크기
     */
    private Integer size;

    /**
     * 추가 데이터 존재 여부
     */
    private Boolean hasNext;

    /**
     * 다음 페이지 조회 시 사용할 커서 (없으면 null)
     */
    private Long nextCursor;
}
