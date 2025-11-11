package com.stdev.smartmealtable.api.food.service;

import com.stdev.smartmealtable.api.food.service.dto.FoodAutocompleteResponse;
import com.stdev.smartmealtable.api.food.service.dto.FoodAutocompleteResponse.FoodSuggestion;
import com.stdev.smartmealtable.api.food.service.dto.FoodTrendingKeywordsResponse;
import com.stdev.smartmealtable.api.food.service.dto.FoodTrendingKeywordsResponse.TrendingKeyword;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.storage.cache.ChosungIndexBuilder;
import com.stdev.smartmealtable.storage.cache.SearchCacheService;
import com.stdev.smartmealtable.support.search.korean.KoreanSearchUtil;
import com.stdev.smartmealtable.support.search.korean.SearchRelevanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 음식 자동완성 서비스
 * 
 * 기능:
 * 1. 실시간 자동완성 (Redis 캐시 기반)
 * 2. 한글 초성 검색 지원
 * 3. 인기 검색어 조회
 * 4. DB Fallback (Redis 장애 시)
 * 5. Store 정보 자동 포함
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
public class FoodAutocompleteService {
    
    private final FoodRepository foodRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final SearchCacheService searchCacheService;
    private final ChosungIndexBuilder chosungIndexBuilder;
    
    private static final String DOMAIN = "food";
    private static final int MAX_TYPO_DISTANCE = 2;
    private static final int MIN_RESULTS_FOR_TYPO = 5;
    
    /**
     * 음식 자동완성
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
    public FoodAutocompleteResponse autocomplete(String keyword, int limit) {
        log.debug("음식 자동완성 요청: keyword={}, limit={}", keyword, limit);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 입력 검증
            if (keyword == null || keyword.trim().isEmpty()) {
                return new FoodAutocompleteResponse(Collections.emptyList());
            }
            
            String normalizedKeyword = keyword.trim();
            
            // 2. 검색 횟수 증가 (인기 검색어 집계)
            searchCacheService.incrementSearchCount(DOMAIN, normalizedKeyword);
            
            // 3. 다단계 검색 전략 실행
            List<Food> results = performMultiStageSearch(normalizedKeyword, limit * 2); // 더 많이 조회 후 정렬

            // 4. 관련성 기준으로 재정렬
            List<Food> sortedResults = sortByRelevance(normalizedKeyword, results, limit);

            // 5. DTO 변환 (Store 정보 포함)
            List<FoodSuggestion> suggestions = sortedResults.stream()
                .map(this::toSuggestion)
                .filter(Objects::nonNull) // Store 조회 실패한 경우 제외
                .collect(Collectors.toList());
            
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("음식 자동완성 완료: keyword={}, results={}, time={}ms", 
                normalizedKeyword, suggestions.size(), elapsedTime);
            
            return new FoodAutocompleteResponse(suggestions);
            
        } catch (Exception e) {
            log.error("음식 자동완성 실패: keyword={}", keyword, e);
            // Fallback: DB 직접 검색
            return fallbackSearch(keyword, limit);
        }
    }
    
    /**
     * 다단계 검색 전략
     * 
     * Stage 1: Prefix 검색 (캐시)
     * Stage 2: 초성 검색 (캐시)
     * Stage 3: 오타 허용 검색 (캐시 + DB)
     * 
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 검색 결과
     */
    private List<Food> performMultiStageSearch(String keyword, int limit) {
        Set<Long> foodIds = new LinkedHashSet<>();
        
        // Stage 1: Prefix 검색 (Redis Sorted Set)
        try {
            List<Long> prefixResults = searchCacheService.getAutocompleteResults(DOMAIN, keyword, limit);
            foodIds.addAll(prefixResults);
            
            if (foodIds.size() >= limit) {
                log.debug("Stage 1 (Prefix) 충분한 결과: {}", foodIds.size());
                return fetchFoods(new ArrayList<>(foodIds));
            }
        } catch (Exception e) {
            log.warn("Stage 1 (Prefix) 검색 실패, Stage 2로 진행", e);
        }
        
        // Stage 2: 초성 검색 (초성 인덱스)
        if (KoreanSearchUtil.isChosung(keyword)) {
            try {
                Set<String> chosungResults = chosungIndexBuilder.findIdsByChosung(DOMAIN, keyword);
                chosungResults.stream()
                    .map(Long::parseLong)
                    .forEach(foodIds::add);
                
                if (foodIds.size() >= limit) {
                    log.debug("Stage 2 (초성) 충분한 결과: {}", foodIds.size());
                    return fetchFoods(new ArrayList<>(foodIds));
                }
            } catch (Exception e) {
                log.warn("Stage 2 (초성) 검색 실패, Stage 3로 진행", e);
            }
        }
        
        // Stage 3: Substring 및 오타 허용 검색 (결과가 부족할 때)
        // PREFIX 검색과 초성 검색으로 결과가 부족하면 더 넓은 검색 수행
        if (foodIds.size() < limit) {
            try {
                List<Food> expandedResults = searchWithTypoTolerance(keyword, limit * 2);
                expandedResults.forEach(food -> {
                    if (!foodIds.contains(food.getFoodId())) {
                        foodIds.add(food.getFoodId());
                    }
                });

                log.debug("Stage 3 (Substring + 오타 허용) 추가 결과: {}", expandedResults.size());
            } catch (Exception e) {
                log.warn("Stage 3 (Substring + 오타 허용) 검색 실패", e);
            }
        }

        return fetchFoods(new ArrayList<>(foodIds));
    }
    
    /**
     * 오타 허용 및 부분 매칭 검색
     *
     * 검색 전략:
     * 1. 키워드를 포함하는 음식명 substring 검색
     * 2. 편집 거리 기반 검색 (첫 2글자 prefix)
     * 3. 결과를 관련성으로 정렬
     *
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 검색 결과
     */
    private List<Food> searchWithTypoTolerance(String keyword, int limit) {
        Set<Long> resultIds = new LinkedHashSet<>();

        // Stage 1: Substring 검색 (키워드를 포함하는 모든 음식)
        // 예: "카츠" 검색 시 "돈카츠", "카츠동", "카츠산도" 모두 매칭
        try {
            List<Food> substringCandidates = foodRepository.findByNameContains(keyword, limit * 3);
            substringCandidates.forEach(food -> resultIds.add(food.getFoodId()));

            if (resultIds.size() >= limit) {
                log.debug("Substring 검색으로 충분한 결과: {}", resultIds.size());
                return fetchFoods(new ArrayList<>(resultIds));
            }
        } catch (Exception e) {
            log.warn("Substring 검색 실패: keyword={}", keyword, e);
        }

        // Stage 2: 편집 거리 기반 검색 (첫 2글자)
        // 예: "고추장" 입력 시 "고추"로 시작하는 음식들 중 거리 2 이내
        if (resultIds.size() < limit && keyword.length() >= 2) {
            try {
                String prefix = keyword.substring(0, Math.min(2, keyword.length()));
                List<Food> typoCandidates = foodRepository.findByNameStartsWith(prefix, limit * 3);

                typoCandidates.stream()
                    .filter(food -> {
                        int distance = KoreanSearchUtil.calculateEditDistance(keyword, food.getFoodName());
                        return distance <= MAX_TYPO_DISTANCE && !resultIds.contains(food.getFoodId());
                    })
                    .forEach(food -> resultIds.add(food.getFoodId()));

                log.debug("편집 거리 검색 추가 결과: {}", typoCandidates.size());
            } catch (Exception e) {
                log.warn("편집 거리 검색 실패: keyword={}", keyword, e);
            }
        }

        // 결과 조회
        return fetchFoods(new ArrayList<>(resultIds));
    }
    
    /**
     * Food ID 목록으로 Food 엔티티 조회
     *
     * @param foodIds Food ID 목록
     * @return Food 엔티티 목록
     */
    private List<Food> fetchFoods(List<Long> foodIds) {
        if (foodIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // 캐시에서 상세 데이터 조회 시도
            List<Food> foods = foodRepository.findAllByIdIn(foodIds);

            // 원래 순서 유지 (대표 메뉴 우선 순서)
            Map<Long, Food> foodMap = foods.stream()
                .collect(Collectors.toMap(Food::getFoodId, f -> f));

            return foodIds.stream()
                .map(foodMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Food 조회 실패", e);
            return Collections.emptyList();
        }
    }

    /**
     * 검색 결과를 관련성 기준으로 재정렬
     *
     * 정렬 규칙 (배달앱 방식):
     * 1순위: 완전 일치 (모든 글자가 포함된 가장 짧은 항목)
     * 2순위: 부분 일치 (일부 글자만 포함)
     * 3순위: 같은 매칭 타입 내에서는 인기도순
     *
     * @param keyword 검색 키워드
     * @param foods 검색 결과 음식 목록
     * @param limit 제한 개수
     * @return 재정렬된 음식 목록
     */
    private List<Food> sortByRelevance(String keyword, List<Food> foods, int limit) {
        if (foods.isEmpty() || keyword == null || keyword.isBlank()) {
            return foods.stream()
                .limit(limit)
                .collect(Collectors.toList());
        }

        return foods.stream()
            .map(food -> new RelevanceScore(
                food,
                SearchRelevanceCalculator.calculateRelevance(
                    keyword,
                    food.getFoodName(),
                    getPopularityScore(food)
                )
            ))
            .filter(score -> score.relevanceScore > 0) // 매칭되지 않은 항목 제외
            .sorted(Comparator.comparingInt(RelevanceScore::getRelevanceScore).reversed()) // 역순 정렬
            .map(RelevanceScore::getFood)
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 인기도 점수 계산 (0~1000)
     *
     * 현재는 대표메뉴 여부로 판단
     * 향후: searchCount, favoriteCount 등을 활용하여 개선
     */
    private int getPopularityScore(Food food) {
        // 대표 메뉴는 기본 점수 500
        if (Boolean.TRUE.equals(food.getIsMain())) {
            return 500;
        }
        return 0;
    }

    /**
     * 내부 클래스: 관련성 점수와 함께 Food 저장
     */
    private static class RelevanceScore {
        private final Food food;
        private final int relevanceScore;

        RelevanceScore(Food food, int relevanceScore) {
            this.food = food;
            this.relevanceScore = relevanceScore;
        }

        Food getFood() {
            return food;
        }

        int getRelevanceScore() {
            return relevanceScore;
        }
    }
    
    /**
     * Fallback 검색 (Redis 장애 시)
     * 
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 자동완성 응답
     */
    private FoodAutocompleteResponse fallbackSearch(String keyword, int limit) {
        log.warn("Fallback 검색 실행: keyword={}", keyword);
        
        try {
            List<Food> foods;
            
            // 초성 검색 여부 확인
            if (KoreanSearchUtil.isChosung(keyword)) {
                // 초성 검색: DB에서 모든 음식 조회 후 필터링
                foods = foodRepository.findByNameStartsWith(keyword.substring(0, 1), limit * 5);
                foods = foods.stream()
                    .filter(food -> KoreanSearchUtil.matchesChosung(keyword, food.getFoodName()))
                    .limit(limit)
                    .collect(Collectors.toList());
            } else {
                // 일반 검색
                foods = foodRepository.findByNameStartsWith(keyword, limit);
            }
            
            List<FoodSuggestion> suggestions = foods.stream()
                .limit(limit)
                .map(this::toSuggestion)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            return new FoodAutocompleteResponse(suggestions);
            
        } catch (Exception e) {
            log.error("Fallback 검색 실패", e);
            return new FoodAutocompleteResponse(Collections.emptyList());
        }
    }
    
    /**
     * 인기 검색어 조회
     * 
     * @param limit 결과 개수
     * @return 인기 검색어 목록
     */
    public FoodTrendingKeywordsResponse getTrendingKeywords(int limit) {
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
            return new FoodTrendingKeywordsResponse(keywords);
            
        } catch (Exception e) {
            log.error("인기 검색어 조회 실패", e);
            return new FoodTrendingKeywordsResponse(Collections.emptyList());
        }
    }
    
    /**
     * Food → FoodSuggestion 변환
     * 
     * @param food Food 엔티티
     * @return FoodSuggestion DTO (Store 조회 실패 시 null)
     */
    private FoodSuggestion toSuggestion(Food food) {
        try {
            // Store 정보 조회 (필수)
            Store store = storeRepository.findById(food.getStoreId())
                .orElse(null);
            
            if (store == null) {
                log.warn("Store 조회 실패, Food 제외: foodId={}, storeId={}", 
                    food.getFoodId(), food.getStoreId());
                return null;
            }
            
            // Category 이름 조회 (선택)
            String categoryName = null;
            if (food.getCategoryId() != null) {
                try {
                    Category category = categoryRepository.findById(food.getCategoryId())
                        .orElse(null);
                    if (category != null) {
                        categoryName = category.getName();
                    }
                } catch (Exception e) {
                    log.warn("카테고리 조회 실패: categoryId={}", food.getCategoryId(), e);
                }
            }
            
            return new FoodSuggestion(
                food.getFoodId(),
                food.getFoodName(),
                store.getStoreId(),
                store.getName(),
                categoryName,
                food.getAveragePrice(),
                food.getIsMain()
            );
            
        } catch (Exception e) {
            log.error("FoodSuggestion 변환 실패: foodId={}", food.getFoodId(), e);
            return null;
        }
    }
}
