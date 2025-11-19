package com.stdev.smartmealtable.api.store.service;

import com.stdev.smartmealtable.api.store.service.dto.StoreAutocompleteResponse;
import com.stdev.smartmealtable.api.store.service.dto.StoreAutocompleteResponse.StoreSuggestion;
import com.stdev.smartmealtable.api.store.service.dto.StoreTrendingKeywordsResponse;
import com.stdev.smartmealtable.api.store.service.dto.StoreTrendingKeywordsResponse.TrendingKeyword;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.storage.cache.ChosungIndexBuilder;
import com.stdev.smartmealtable.storage.cache.KeywordRankingCacheService;
import com.stdev.smartmealtable.storage.cache.SearchCacheService;
import com.stdev.smartmealtable.storage.db.search.SearchKeywordSupport;
import com.stdev.smartmealtable.support.search.korean.KoreanSearchUtil;
import com.stdev.smartmealtable.support.search.korean.SearchRelevanceCalculator;
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
    private final KeywordRankingCacheService keywordRankingCacheService;
    
    private static final String DOMAIN = "store";
    private static final int MAX_TYPO_DISTANCE = 2;
    private static final int MIN_RESULTS_FOR_TYPO = 5;
    private static final int RANKING_PREFIX_LENGTH = 2;
    
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
            List<Store> results = performMultiStageSearch(normalizedKeyword, limit * 2); // 더 많이 조회 후 정렬

            // 4. 관련성 기준으로 재정렬
            List<Store> sortedResults = sortByRelevance(normalizedKeyword, results, limit);

            // 5. DTO 변환 (카테고리 이름 포함)
            List<StoreSuggestion> suggestions = sortedResults.stream()
                .map(this::toSuggestion)
                .collect(Collectors.toList());

            suggestions = applyRankingBoost(normalizedKeyword, suggestions);
            
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
        
        // Stage 3: Substring 및 오타 허용 검색 (결과가 부족할 때)
        // PREFIX 검색과 초성 검색으로 결과가 부족하면 더 넓은 검색 수행
        if (storeIds.size() < limit) {
            try {
                List<Store> expandedResults = searchWithTypoTolerance(keyword, limit * 2);
                expandedResults.forEach(store -> {
                    if (!storeIds.contains(store.getStoreId())) {
                        storeIds.add(store.getStoreId());
                    }
                });

                log.debug("Stage 3 (Substring + 오타 허용) 추가 결과: {}", expandedResults.size());
            } catch (Exception e) {
                log.warn("Stage 3 (Substring + 오타 허용) 검색 실패", e);
            }
        }

        return fetchStores(new ArrayList<>(storeIds));
    }
    
    /**
     * 오타 허용 검색 + 카테고리 검색
     *
     * 검색 전략:
     * 1. 가게명 Prefix 검색
     * 2. 가게명 Substring 검색
     * 3. 카테고리명 검색
     * 4. 편집 거리 기반 Fallback 검색
     *
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 검색 결과
     */
    private List<Store> searchWithTypoTolerance(String keyword, int limit) {
        Set<Long> resultIds = new LinkedHashSet<>();

        // Stage 1: 가게명 Prefix 검색 (최고 우선순위)
        List<Store> prefixCandidates = storeRepository.findByNameStartsWith(keyword, limit * 2);
        prefixCandidates.forEach(store -> resultIds.add(store.getStoreId()));

        if (resultIds.size() >= limit) {
            return prefixCandidates.stream().limit(limit).collect(Collectors.toList());
        }

        // Stage 2: 가게명 Substring 검색 (이름 중간에 있는 키워드)
        if (resultIds.size() < limit) {
            List<Store> substringCandidates = storeRepository.findByNameContains(keyword, limit * 3);
            for (Store store : substringCandidates) {
                if (resultIds.size() >= limit) break;
                resultIds.add(store.getStoreId());
            }
        }

        // Stage 3: 카테고리명 검색
        if (resultIds.size() < limit) {
            try {
                List<Long> categoryIds = categoryRepository.findByNameContains(keyword, limit);
                List<Long> storesToAdd = categoryRepository.findStoreIdsByCategories(categoryIds, limit);
                for (Long storeId : storesToAdd) {
                    if (resultIds.size() >= limit) break;
                    resultIds.add(storeId);
                }
            } catch (Exception e) {
                log.warn("카테고리 검색 실패: keyword={}", keyword, e);
            }
        }

        // Stage 4: 편집 거리 기반 검색 (Fallback)
        if (resultIds.size() < limit) {
            String prefix = keyword.substring(0, Math.min(2, keyword.length()));
            List<Store> typoCandidates = storeRepository.findByNameStartsWith(prefix, limit * 3);

            typoCandidates.stream()
                .filter(store -> {
                    int distance = KoreanSearchUtil.calculateEditDistance(keyword, store.getName());
                    return distance <= MAX_TYPO_DISTANCE;
                })
                .sorted(Comparator.comparingInt(store ->
                    KoreanSearchUtil.calculateEditDistance(keyword, store.getName())
                ))
                .forEach(store -> {
                    if (resultIds.size() >= limit) return;
                    resultIds.add(store.getStoreId());
                });
        }

        // 결과 조회
        return resultIds.stream()
            .limit(limit)
            .map(storeId -> {
                var storeOpt = storeRepository.findById(storeId);
                return storeOpt.orElse(null);
            })
            .filter(store -> store != null)
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
     * 검색 결과를 관련성 기준으로 재정렬
     *
     * 정렬 규칙 (배달앱 방식):
     * 1순위: 완전 일치 (모든 글자가 포함된 가장 짧은 항목)
     * 2순위: 부분 일치 (일부 글자만 포함)
     * 3순위: 같은 매칭 타입 내에서는 인기도순 (favoriteCount)
     *
     * @param keyword 검색 키워드
     * @param stores 검색 결과 가게 목록
     * @param limit 제한 개수
     * @return 재정렬된 가게 목록
     */
    private List<Store> sortByRelevance(String keyword, List<Store> stores, int limit) {
        if (stores.isEmpty() || keyword == null || keyword.isBlank()) {
            return stores.stream()
                .limit(limit)
                .collect(Collectors.toList());
        }

        return stores.stream()
            .map(store -> new RelevanceScore(
                store,
                SearchRelevanceCalculator.calculateRelevance(
                    keyword,
                    store.getName(),
                    store.getFavoriteCount() != null ? store.getFavoriteCount() : 0
                )
            ))
            .filter(score -> score.relevanceScore > 0) // 매칭되지 않은 항목 제외
            .sorted(Comparator.comparingInt(RelevanceScore::getRelevanceScore).reversed()) // 역순 정렬
            .map(RelevanceScore::getStore)
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 내부 클래스: 관련성 점수와 함께 Store 저장
     */
    private static class RelevanceScore {
        private final Store store;
        private final int relevanceScore;

        RelevanceScore(Store store, int relevanceScore) {
            this.store = store;
            this.relevanceScore = relevanceScore;
        }

        Store getStore() {
            return store;
        }

        int getRelevanceScore() {
            return relevanceScore;
        }
    }

    private List<StoreSuggestion> applyRankingBoost(String keyword, List<StoreSuggestion> suggestions) {
        if (suggestions.isEmpty()) {
            return suggestions;
        }
        String prefix = extractRankingPrefix(keyword);
        if (prefix.isEmpty()) {
            return suggestions;
        }

        List<String> rankingKeywords = keywordRankingCacheService.getTopKeywords(prefix, suggestions.size());
        if (rankingKeywords.isEmpty()) {
            return suggestions;
        }

        List<StoreSuggestion> ordered = new ArrayList<>();
        Set<StoreSuggestion> seen = new LinkedHashSet<>();

        for (String rankingKeyword : rankingKeywords) {
            for (StoreSuggestion suggestion : suggestions) {
                if (seen.contains(suggestion)) {
                    continue;
                }
                String normalizedName = SearchKeywordSupport.normalize(suggestion.name());
                if (normalizedName.startsWith(rankingKeyword)) {
                    ordered.add(suggestion);
                    seen.add(suggestion);
                }
            }
        }

        for (StoreSuggestion suggestion : suggestions) {
            if (seen.add(suggestion)) {
                ordered.add(suggestion);
            }
        }

        return ordered;
    }

    private String extractRankingPrefix(String keyword) {
        String normalized = SearchKeywordSupport.normalize(keyword);
        if (normalized.isEmpty()) {
            return "";
        }
        return normalized.substring(0, Math.min(RANKING_PREFIX_LENGTH, normalized.length()));
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
