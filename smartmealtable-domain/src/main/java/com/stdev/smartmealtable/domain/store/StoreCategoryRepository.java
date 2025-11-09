package com.stdev.smartmealtable.domain.store;

import java.util.List;

/**
 * Store-Category N:N 관계를 관리하는 Repository 인터페이스
 * 가게가 가지고 있는 여러 카테고리와의 관계를 저장/조회
 */
public interface StoreCategoryRepository {
    
    /**
     * 가게-카테고리 관계를 저장
     * 
     * @param storeId 가게 ID
     * @param categoryId 카테고리 ID
     * @param displayOrder 표시 순서
     */
    void save(Long storeId, Long categoryId, int displayOrder);
    
    /**
     * 특정 가게에 해당하는 모든 카테고리 ID 조회
     * 
     * @param storeId 가게 ID
     * @return 카테고리 ID 리스트 (표시 순서로 정렬됨)
     */
    List<Long> findCategoryIdsByStoreId(Long storeId);
    
    /**
     * 특정 가게의 모든 카테고리 관계 삭제
     * 
     * @param storeId 가게 ID
     */
    void deleteByStoreId(Long storeId);
    
    /**
     * 특정 가게-카테고리 관계의 존재 여부 확인
     * 
     * @param storeId 가게 ID
     * @param categoryId 카테고리 ID
     * @return 존재 여부
     */
    boolean existsByStoreIdAndCategoryId(Long storeId, Long categoryId);
}
