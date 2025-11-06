package com.stdev.smartmealtable.domain.store;

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
}
