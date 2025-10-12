package com.stdev.smartmealtable.domain.store;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 가게(Store) Repository 인터페이스
 */
public interface StoreRepository {
    
    /**
     * 가게 ID로 조회
     */
    Optional<Store> findById(Long storeId);
    
    /**
     * 가게 ID로 조회 (삭제된 가게 제외)
     */
    Optional<Store> findByIdAndDeletedAtIsNull(Long storeId);
    
    /**
     * 여러 가게 ID로 조회
     */
    List<Store> findByIdIn(List<Long> storeIds);
    
    /**
     * 가게 저장
     */
    Store save(Store store);
    
    /**
     * 키워드로 가게명 또는 카테고리명 검색 (자동완성용)
     * @param keyword 검색 키워드
     * @param limit 결과 제한 수
     * @return 검색 결과 리스트
     */
    List<Store> searchByKeywordForAutocomplete(String keyword, int limit);
    
    /**
     * 조건에 맞는 가게 목록 조회 (복잡한 필터링 및 정렬 지원)
     * QueryDSL을 활용하여 동적 쿼리 구현
     * 
     * @param keyword 검색 키워드 (가게명 또는 카테고리명)
     * @param userLatitude 사용자 위도 (거리 계산용)
     * @param userLongitude 사용자 경도 (거리 계산용)
     * @param radiusKm 검색 반경 (km)
     * @param categoryId 카테고리 필터
     * @param isOpenOnly 영업 중만 조회 여부
     * @param storeType 가게 유형 필터
     * @param sortBy 정렬 기준
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 가게 목록과 총 개수
     */
    StoreSearchResult searchStores(
            String keyword,
            BigDecimal userLatitude,
            BigDecimal userLongitude,
            Double radiusKm,
            Long categoryId,
            Boolean isOpenOnly,
            StoreType storeType,
            String sortBy,
            int page,
            int size
    );
    
    /**
     * 가게 검색 결과를 담는 레코드
     */
    record StoreSearchResult(
            List<Store> stores,
            long totalCount
    ) {
    }
}
