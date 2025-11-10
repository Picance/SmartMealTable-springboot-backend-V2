package com.stdev.smartmealtable.api.store.service;

import com.stdev.smartmealtable.api.store.service.dto.StoreAutocompleteResponse;
import com.stdev.smartmealtable.api.store.service.dto.StoreAutocompleteResponse.StoreSuggestion;
import com.stdev.smartmealtable.api.store.service.dto.StoreTrendingKeywordsResponse;
import com.stdev.smartmealtable.api.store.service.dto.StoreTrendingKeywordsResponse.TrendingKeyword;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.support.search.cache.ChosungIndexBuilder;
import com.stdev.smartmealtable.support.search.cache.SearchCacheService;
import com.stdev.smartmealtable.support.search.korean.KoreanSearchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 가게 자동완성 서비스
 * 
 * 기능:
 * 1. 실시간 자동완성 (Redis 캐시 기반)
 * 2. 한글 초성 검색 지원
 * 3. 인기 검색어 조회
 * 4. DB Fallback (Redis 장애 시)
 * 
 * 성능 목표:
 * - p95 latency < 100ms (캐시 히트)
 * - p95 latency < 500ms (캐시 미스 + DB fallback)
 * 
 * @author SmartMealTable Team
 * @since 2025-11-10
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StoreAutocompleteService {
    
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final SearchCacheService searchCacheService;
    private final ChosungIndexBuilder chosungIndexBuilder;
    
    private static final String DOMAIN = "store";
    private static final int MAX_TYPO_DISTANCE = 2;
    private static final int MIN_RESULTS_FOR_TYPO = 5;
    
    /**
     * 가게 자동완성
     * 
     * 검색 전략:
     * 1. Redis 캐시에서 prefix 검색
     * 2. 결과가 없으면 초성 검색
     * 3. 여전히 부족하면 오타 허용 검색
     * 4. Redis 실패 시 DB fallback
     * 
     * @param keyword 검색 키워드
     * @param limit 결과 개수 제한
     * @return 자동완성 제안 목록
     */
    public StoreAutocompleteResponse autocomplete(String keyword, int limit) {
        log.debug("가게 자동완성 요청: keyword={}, limit={}", keyword, limit);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 입력 검증
            if (keyword == null || keyword.trim().isEmpty()) {
                return new StoreAutocompleteResponse(Collections.emptyList());
            }
            
            String normalizedKeyword = keyword.trim();
            
            // 2. 검색 횟수 증가 (인기 검색어 집계)
            searchCacheService.incrementSearchCount(DOMAIN, normalizedKeyword);
            
            // 3. 다단계 검색 전략 실행
            List<Store> results = performMultiStageSearch(normalizedKeyword, limit);
            
            // 4. DTO 변환 (카테고리 이름 포함)
            List<StoreSuggestion> suggestions = results.stream()
                .limit(limit)
                .map(this::toSuggestion)
                .collect(Collectors.toList());
            
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("가게 자동완성 완료: keyword={}, results={}, time={}ms", 
                normalizedKeyword, suggestions.size(), elapsedTime);
            
            return new StoreAutocompleteResponse(suggestions);
            
        } catch (Exception e) {
            log.error("가게 자동완성 실패: keyword={}", keyword, e);
            // Fallback: DB 직접 검색
            return fallbackSearch(keyword, limit);
        }
    }
    
    /**
     * 다단계 검색 전략
     * 
     * Stage 1: Prefix 검색 (캐시)
     * Stage 2: 초성 검색 (캐시)
     * Stage 3: DB Fallback (캐시 미스 시)
     * 
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 검색 결과
     */
    private List<Store> performMultiStageSearch(String keyword, int limit) {
        Set<Long> storeIds = new LinkedHashSet<>();
        
        // Stage 1: Prefix 검색 (Redis Sorted Set)
        try {
            List<Long> prefixResults = searchCacheService.getAutocompleteResults(DOMAIN, keyword, limit);
            storeIds.addAll(prefixResults);
            
            if (storeIds.size() >= limit) {
                log.debug("Stage 1 (Prefix) 충분한 결과: {}", storeIds.size());
                return fetchStores(new ArrayList<>(storeIds));
            }
        } catch (Exception e) {
            log.warn("Stage 1 (Prefix) 검색 실패, DB Fallback으로 진행", e);
        }
        
        // Stage 2: 초성 검색 (초성 인덱스)
        if (KoreanSearchUtil.isChosung(keyword)) {
            try {
                Set<String> chosungResults = chosungIndexBuilder.findIdsByChosung(DOMAIN, keyword);
                chosungResults.stream()
                    .map(Long::parseLong)
                    .forEach(storeIds::add);
                
                if (storeIds.size() >= limit) {
                    log.debug("Stage 2 (초성) 충분한 결과: {}", storeIds.size());
                    return fetchStores(new ArrayList<>(storeIds));
                }
            } catch (Exception e) {
                log.warn("Stage 2 (초성) 검색 실패, DB Fallback으로 진행", e);
            }
        }
        
        // Stage 3: DB Fallback (캐시 미스 또는 결과 부족)
        // Redis가 빈 결과를 반환했거나, 결과가 부족한 경우 DB에서 직접 검색
        if (storeIds.isEmpty() || storeIds.size() < MIN_RESULTS_FOR_TYPO) {
            log.info("캐시 미스 또는 결과 부족, DB Fallback 검색 실행: keyword={}", keyword);
            return searchWithTypoTolerance(keyword, limit);
        }
        
        return fetchStores(new ArrayList<>(storeIds));
    }
    
    /**
     * 오타 허용 검색
     * 
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 검색 결과
     */
    private List<Store> searchWithTypoTolerance(String keyword, int limit) {
        // DB에서 prefix 검색
        List<Store> candidates = storeRepository.findByNameStartsWith(keyword, limit * 2);
        
        // Prefix 매칭 결과가 충분하면 즉시 반환
        if (!candidates.isEmpty()) {
            return candidates.stream().limit(limit).collect(Collectors.toList());
        }
        
        // Prefix 매칭 실패 시, 편집 거리 기반 검색 (첫 2글자)
        String prefix = keyword.substring(0, Math.min(2, keyword.length()));
        candidates = storeRepository.findByNameStartsWith(prefix, limit * 3);
        
        return candidates.stream()
            .filter(store -> {
                int distance = KoreanSearchUtil.calculateEditDistance(keyword, store.getName());
                return distance <= MAX_TYPO_DISTANCE;
            })
            .sorted(Comparator.comparingInt(store -> 
                KoreanSearchUtil.calculateEditDistance(keyword, store.getName())
            ))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Store ID 목록으로 Store 엔티티 조회
     * 
     * @param storeIds Store ID 목록
     * @return Store 엔티티 목록
     */
    private List<Store> fetchStores(List<Long> storeIds) {
        if (storeIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            // 캐시에서 상세 데이터 조회 시도
            List<Store> stores = storeRepository.findAllByIdIn(storeIds);
            
            // 원래 순서 유지 (popularity 순서)
            Map<Long, Store> storeMap = stores.stream()
                .collect(Collectors.toMap(Store::getStoreId, s -> s));
            
            return storeIds.stream()
                .map(storeMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Store 조회 실패", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Fallback 검색 (Redis 장애 시)
     * 
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 자동완성 응답
     */
    private StoreAutocompleteResponse fallbackSearch(String keyword, int limit) {
        log.warn("Fallback 검색 실행: keyword={}", keyword);
        
        try {
            List<Store> stores;
            
            // 초성 검색 여부 확인
            if (KoreanSearchUtil.isChosung(keyword)) {
                // 초성 검색: DB에서 모든 가게 조회 후 필터링
                stores = storeRepository.findByNameStartsWith(keyword.substring(0, 1), limit * 5);
                stores = stores.stream()
                    .filter(store -> KoreanSearchUtil.matchesChosung(keyword, store.getName()))
                    .limit(limit)
                    .collect(Collectors.toList());
            } else {
                // 일반 검색
                stores = storeRepository.findByNameStartsWith(keyword, limit);
            }
            
            List<StoreSuggestion> suggestions = stores.stream()
                .limit(limit)
                .map(this::toSuggestion)
                .collect(Collectors.toList());
            
            return new StoreAutocompleteResponse(suggestions);
            
        } catch (Exception e) {
            log.error("Fallback 검색 실패", e);
            return new StoreAutocompleteResponse(Collections.emptyList());
        }
    }
    
    /**
     * 인기 검색어 조회
     * 
     * @param limit 결과 개수
     * @return 인기 검색어 목록
     */
    public StoreTrendingKeywordsResponse getTrendingKeywords(int limit) {
        log.debug("인기 검색어 조회: limit={}", limit);
        
        try {
            List<SearchCacheService.TrendingKeyword> trending = 
                searchCacheService.getTrendingKeywords(DOMAIN, limit);
            
            List<TrendingKeyword> keywords = new ArrayList<>();
            for (int i = 0; i < trending.size(); i++) {
                SearchCacheService.TrendingKeyword tk = trending.get(i);
                keywords.add(new TrendingKeyword(
                    tk.keyword(),
                    tk.searchCount(),
                    i + 1 // 순위
                ));
            }
            
            log.info("인기 검색어 조회 완료: count={}", keywords.size());
            return new StoreTrendingKeywordsResponse(keywords);
            
        } catch (Exception e) {
            log.error("인기 검색어 조회 실패", e);
            return new StoreTrendingKeywordsResponse(Collections.emptyList());
        }
    }
    
    /**
     * Store → StoreSuggestion 변환
     * 
     * @param store Store 엔티티
     * @return StoreSuggestion DTO
     */
    private StoreSuggestion toSuggestion(Store store) {
        // Category ID -> Category Name 변환
        List<String> categoryNames = Collections.emptyList();
        if (store.getCategoryIds() != null && !store.getCategoryIds().isEmpty()) {
            try {
                List<Category> categories = categoryRepository.findByIdIn(store.getCategoryIds());
                categoryNames = categories.stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("카테고리 조회 실패: storeId={}", store.getStoreId(), e);
            }
        }
        
        return new StoreSuggestion(
            store.getStoreId(),
            store.getName(),
            store.getStoreType(),
            store.getAddress(),
            categoryNames
        );
    }
}
