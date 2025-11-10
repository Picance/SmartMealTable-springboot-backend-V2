package com.stdev.smartmealtable.api.search.config;

import com.stdev.smartmealtable.api.search.service.SearchCacheWarmingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 캐시 워밍을 수행하는 Runner
 * <p>
 * 서버가 시작되면 자동으로 Store, Food, Group 데이터를 Redis에 사전 로드하여
 * 첫 검색 요청부터 빠른 응답 속도를 보장합니다.
 * </p>
 * 
 * @see SearchCacheWarmingService
 */
@Slf4j
@Component
@Profile("!test")  // 테스트 환경에서는 실행하지 않음
@RequiredArgsConstructor
public class CacheWarmingRunner implements ApplicationRunner {

    private final SearchCacheWarmingService cacheWarmingService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("===== 애플리케이션 시작: 캐시 워밍 시작 =====");
        
        try {
            cacheWarmingService.warmAllCaches();
            log.info("===== 애플리케이션 시작: 캐시 워밍 성공 =====");
        } catch (Exception e) {
            log.error("===== 애플리케이션 시작: 캐시 워밍 실패 =====", e);
            // 캐시 워밍 실패해도 서버는 계속 실행 (DB Fallback 존재)
        }
    }
}
