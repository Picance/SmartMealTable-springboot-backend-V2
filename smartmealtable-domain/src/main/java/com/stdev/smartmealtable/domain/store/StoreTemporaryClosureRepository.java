package com.stdev.smartmealtable.domain.store;

import java.time.LocalDate;
import java.util.List;

/**
 * 가게 임시 휴무 Repository 인터페이스
 */
public interface StoreTemporaryClosureRepository {
    
    /**
     * 가게 ID와 휴무 날짜로 임시 휴무 목록 조회
     */
    List<StoreTemporaryClosure> findByStoreIdAndClosureDate(Long storeId, LocalDate closureDate);
    
    /**
     * 가게 ID로 모든 임시 휴무 목록 조회
     */
    List<StoreTemporaryClosure> findByStoreId(Long storeId);
    
    /**
     * 임시 휴무 저장
     */
    StoreTemporaryClosure save(StoreTemporaryClosure storeTemporaryClosure);
}
