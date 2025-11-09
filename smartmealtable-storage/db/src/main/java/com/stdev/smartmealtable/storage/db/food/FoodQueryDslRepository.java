package com.stdev.smartmealtable.storage.db.food;

import com.stdev.smartmealtable.domain.food.FoodPageResult;

/**
 * Food QueryDSL Repository 인터페이스
 */
public interface FoodQueryDslRepository {

    /**
     * 관리자용 음식 검색 (페이징, 삭제되지 않은 것만)
     * 
     * @param categoryId 카테고리 ID (선택)
     * @param storeId 가게 ID (선택)
     * @param name 검색할 이름 (부분 일치, 선택)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 페이징된 음식 결과 (삭제되지 않은 것만)
     */
    FoodPageResult adminSearch(Long categoryId, Long storeId, String name, int page, int size);

    /**
     * 카테고리가 음식에서 사용 중인지 확인 (삭제되지 않은 것만)
     */
    boolean existsByCategoryIdAndNotDeleted(Long categoryId);

    /**
     * 가게가 음식에서 사용 중인지 확인 (삭제되지 않은 것만)
     */
    boolean existsByStoreIdAndNotDeleted(Long storeId);
    
    // ===== 자동완성 전용 메서드 (Phase 3) =====
    
    /**
     * 음식명 Prefix로 시작하는 음식 조회 (자동완성용)
     * 삭제되지 않은 음식만 조회하며, 대표 메뉴 우선, 이름 순으로 정렬
     * 
     * @param prefix 검색 접두사
     * @param limit 결과 제한 수
     * @return 음식 리스트
     */
    java.util.List<FoodJpaEntity> findByNameStartingWith(String prefix, int limit);
    
    /**
     * 여러 음식 ID로 조회 (캐시에서 가져온 ID로 조회)
     * 삭제되지 않은 음식만 조회
     * 
     * @param foodIds 음식 ID 리스트
     * @return 음식 리스트
     */
    java.util.List<FoodJpaEntity> findByFoodIdIn(java.util.List<Long> foodIds);
}
