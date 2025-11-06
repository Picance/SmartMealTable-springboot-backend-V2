package com.stdev.smartmealtable.domain.store;

import java.util.List;

/**
 * 가게 영업시간 Repository 인터페이스
 */
public interface StoreOpeningHourRepository {
    
    /**
     * 가게 ID로 영업시간 목록 조회
     */
    List<StoreOpeningHour> findByStoreId(Long storeId);
    
    /**
     * 영업시간 저장
     */
    StoreOpeningHour save(StoreOpeningHour storeOpeningHour);
    
    /**
     * 가게의 모든 영업시간 삭제
     */
    void deleteByStoreId(Long storeId);
}
