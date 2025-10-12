package com.stdev.smartmealtable.domain.store;

import java.time.LocalDateTime;

/**
 * 가게 조회 이력 Repository 인터페이스
 */
public interface StoreViewHistoryRepository {
    
    /**
     * 조회 이력 저장
     */
    StoreViewHistory save(StoreViewHistory storeViewHistory);
    
    /**
     * 조회 이력 생성
     */
    default StoreViewHistory createViewHistory(Long storeId, Long memberId) {
        return new StoreViewHistory(
                null,
                storeId,
                memberId,
                LocalDateTime.now()
        );
    }
}
