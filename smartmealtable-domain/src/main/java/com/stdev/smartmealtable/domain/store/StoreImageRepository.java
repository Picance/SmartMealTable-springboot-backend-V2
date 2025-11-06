package com.stdev.smartmealtable.domain.store;

import java.util.List;
import java.util.Optional;

/**
 * 가게 이미지 Repository 인터페이스
 */
public interface StoreImageRepository {
    
    /**
     * 이미지 저장
     */
    StoreImage save(StoreImage storeImage);
    
    /**
     * 이미지 ID로 조회
     */
    StoreImage findById(Long storeImageId);
    
    /**
     * 가게의 모든 이미지 삭제
     */
    void deleteByStoreId(Long storeId);
    
    /**
     * 가게의 모든 이미지 조회 (isMain 우선, displayOrder 순 정렬)
     */
    List<StoreImage> findByStoreId(Long storeId);
    
    /**
     * 가게의 대표 이미지 조회
     */
    Optional<StoreImage> findByStoreIdAndIsMainTrue(Long storeId);
    
    /**
     * 가게의 첫 번째 이미지 조회 (displayOrder 오름차순)
     */
    Optional<StoreImage> findFirstByStoreIdOrderByDisplayOrderAsc(Long storeId);
}
