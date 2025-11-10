package com.stdev.smartmealtable.api.search.config;

import com.stdev.smartmealtable.api.search.service.SearchCacheWarmingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 캐시 갱신 스케줄러
 * <p>
 * 매일 새벽 3시에 Redis 캐시를 갱신하여 최신 데이터를 유지합니다.
 * 데이터가 추가/수정/삭제되더라도 스케줄러를 통해 자동으로 캐시가 동기화됩니다.
 * </p>
 * 
 * @see SearchCacheWarmingService
 */
@Slf4j
@Configuration
@EnableScheduling
@Profile("!test")  // 테스트 환경에서는 실행하지 않음
@RequiredArgsConstructor
public class CacheRefreshScheduler {

    private final SearchCacheWarmingService cacheWarmingService;

    /**
     * 매일 새벽 3시에 전체 캐시를 갱신합니다.
     * <p>
     * Cron 표현식: "0 0 3 * * *"
     * - 초: 0
     * - 분: 0
     * - 시: 3 (새벽 3시)
     * - 일: * (매일)
     * - 월: * (매월)
     * - 요일: * (모든 요일)
     * </p>
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void refreshCache() {
        log.info("===== 스케줄 캐시 갱신 시작 (매일 새벽 3시) =====");
        
        try {
            cacheWarmingService.warmAllCaches();
            log.info("===== 스케줄 캐시 갱신 완료 =====");
        } catch (Exception e) {
            log.error("===== 스케줄 캐시 갱신 실패 =====", e);
        }
    }
}
