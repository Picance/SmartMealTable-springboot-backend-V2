package com.stdev.smartmealtable.domain.food;

import java.util.List;
import java.util.Optional;

/**
 * 음식 Repository 인터페이스
 */
public interface FoodRepository {

    /**
     * 음식 저장
     */
    Food save(Food food);

    /**
     * 음식 ID로 조회
     */
    Optional<Food> findById(Long foodId);

    /**
     * 여러 음식 ID로 조회
     */
    List<Food> findByIdIn(List<Long> foodIds);

    /**
     * 모든 음식 조회 (페이징은 Storage 계층에서 처리)
     */
    List<Food> findAll(int page, int size);

    /**
     * 카테고리별 음식 조회 (페이징은 Storage 계층에서 처리)
     */
    List<Food> findByCategoryId(Long categoryId, int page, int size);

    /**
     * 전체 음식 개수 조회
     */
    long count();

    /**
     * 카테고리별 음식 개수 조회
     */
    long countByCategoryId(Long categoryId);

    /**
     * 가게별 음식 조회
     */
    List<Food> findByStoreId(Long storeId);

    /**
     * 가게별 음식 삭제 (크롤러 배치용)
     */
    void deleteByStoreId(Long storeId);

    // ===== ADMIN 전용 메서드 =====

    /**
     * 음식 ID로 조회 (삭제된 음식 제외)
     */
    Optional<Food> findByIdAndDeletedAtIsNull(Long foodId);

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
     * 음식 삭제 (논리적 삭제 - deleted_at 설정)
     */
    void softDelete(Long foodId);

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
     * 
     * @param prefix 검색 접두사 (예: "김치")
     * @param limit 결과 제한 수
     * @return 음식 리스트 (대표 메뉴 우선, 이름 순으로 정렬)
     */
    List<Food> findByNameStartsWith(String prefix, int limit);
    
    /**
     * 여러 음식 ID로 조회 (캐시에서 가져온 ID로 조회)
     * 
     * @param foodIds 음식 ID 리스트
     * @return 음식 리스트
     */
    List<Food> findAllByIdIn(List<Long> foodIds);
    
    /**
     * 카테고리 정보를 포함하여 모든 음식 조회 (캐시 워밍용)
     * 
     * @return 음식 리스트 (카테고리 포함)
     */
    List<Food> findAllWithCategories();
}
