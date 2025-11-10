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
     * 외부 크롤링 ID로 조회
     * 배치 작업에서 기존 가게 찾기에 사용
     */
    Optional<Store> findByExternalId(String externalId);
    
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
    
    // ===== 자동완성 전용 메서드 (Phase 3) =====
    
    /**
     * 가게명 Prefix로 시작하는 가게 조회 (자동완성용)
     * 
     * @param prefix 검색 접두사 (예: "서울")
     * @param limit 결과 제한 수
     * @return 가게 리스트 (popularity 높은 순으로 정렬)
     */
    List<Store> findByNameStartsWith(String prefix, int limit);
    
    /**
     * 여러 가게 ID로 조회 (캐시에서 가져온 ID로 조회)
     * 
     * @param storeIds 가게 ID 리스트
     * @return 가게 리스트
     */
    List<Store> findAllByIdIn(List<Long> storeIds);
    
    /**
     * 전체 가게 개수 조회 (캐시 워밍용)
     * 
     * @return 가게 개수
     */
    long count();
    
    /**
     * 페이징으로 모든 가게 조회 (캐시 워밍용)
     * 
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 가게 리스트
     */
    List<Store> findAll(int page, int size);
    
    /**
     * 카테고리 정보를 포함하여 모든 가게 조회 (캐시 워밍용)
     * 
     * @return 가게 리스트 (카테고리 포함)
     */
    List<Store> findAllWithCategories();
    
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
     * @return 거리 정보를 포함한 가게 목록과 총 개수
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
            List<StoreWithDistance> stores,
            long totalCount
    ) {
    }

    // ===== ADMIN 전용 메서드 =====

    /**
     * 관리자용 음식점 검색 (페이징, 삭제되지 않은 것만)
     * 
     * @param categoryId 카테고리 ID (선택)
     * @param name 검색할 이름 (부분 일치, 선택)
     * @param storeType 음식점 유형 (선택)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 페이징된 음식점 결과 (삭제되지 않은 것만)
     */
    StorePageResult adminSearch(Long categoryId, String name, StoreType storeType, int page, int size);

    /**
     * 음식점 삭제 (논리적 삭제 - deleted_at 설정)
     */
    void softDelete(Long storeId);

    /**
     * 카테고리가 음식점에서 사용 중인지 확인 (삭제되지 않은 것만)
     */
    boolean existsByCategoryIdAndNotDeleted(Long categoryId);

    // ===== StoreOpeningHour 관련 =====

    /**
     * 영업시간 저장
     */
    StoreOpeningHour saveOpeningHour(StoreOpeningHour openingHour);

    /**
     * 특정 음식점의 모든 영업시간 조회
     */
    List<StoreOpeningHour> findOpeningHoursByStoreId(Long storeId);

    /**
     * 영업시간 ID로 조회
     */
    Optional<StoreOpeningHour> findOpeningHourById(Long storeOpeningHourId);

    /**
     * 영업시간 삭제 (물리적)
     */
    void deleteOpeningHourById(Long storeOpeningHourId);

    // ===== StoreTemporaryClosure 관련 =====

    /**
     * 임시 휴무 저장
     */
    StoreTemporaryClosure saveTemporaryClosure(StoreTemporaryClosure temporaryClosure);

    /**
     * 특정 음식점의 모든 임시 휴무 조회
     */
    List<StoreTemporaryClosure> findTemporaryClosuresByStoreId(Long storeId);

    /**
     * 임시 휴무 ID로 조회
     */
    Optional<StoreTemporaryClosure> findTemporaryClosureById(Long storeTemporaryClosureId);

    /**
     * 임시 휴무 삭제 (물리적)
     */
    void deleteTemporaryClosureById(Long storeTemporaryClosureId);
}
