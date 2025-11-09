package com.stdev.smartmealtable.support.search.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 검색 캐시 서비스
 * 
 * 기능:
 * 1. 자동완성 캐시 관리 (Sorted Set)
 * 2. 엔티티 상세 데이터 캐시 (Hash)
 * 3. 인기 검색어 관리 (Sorted Set)
 * 4. 관련 검색어 추천 (Set)
 * 
 * Redis 데이터 구조:
 * - autocomplete:{domain}:{prefix} (Sorted Set): 자동완성용, Score = popularity
 * - {domain}:detail:{id} (Hash): 상세 데이터
 * - trending:{domain} (Sorted Set): 인기 검색어, Score = search count
 * - related:{keyword} (Set): 관련 검색어
 * 
 * @author SmartMealTable Team
 * @since 2025-11-09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchCacheService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final long TTL_HOURS = 24;
    private static final int MAX_PREFIX_LENGTH = 2; // Prefix 최대 길이 (키 폭발 방지)
    
    // ==================== 자동완성 캐시 ====================
    
    /**
     * 자동완성 데이터 캐시에 저장
     * 
     * @param domain 도메인 (예: "group", "recommendation")
     * @param entities 캐시할 엔티티 목록
     */
    public void cacheAutocompleteData(String domain, List<AutocompleteEntity> entities) {
        log.info("===== 자동완성 캐시 저장 시작: domain={}, entities={} =====", domain, entities.size());
        
        long startTime = System.currentTimeMillis();
        Map<String, Integer> keyCount = new HashMap<>();
        
        for (AutocompleteEntity entity : entities) {
            // 1-2자 prefix로 자동완성 키 생성
            Set<String> prefixes = generatePrefixes(entity.name());
            
            for (String prefix : prefixes) {
                String key = buildAutocompleteKey(domain, prefix);
                
                // Sorted Set에 추가 (Score = popularity)
                redisTemplate.opsForZSet().add(key, String.valueOf(entity.id()), entity.popularity());
                
                keyCount.merge(key, 1, Integer::sum);
            }
            
            // 상세 데이터 캐시
            cacheDetailData(domain, entity.id(), entity.attributes());
        }
        
        // TTL 일괄 설정
        for (String key : keyCount.keySet()) {
            redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
        }
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        
        log.info("===== 자동완성 캐시 저장 완료: domain={}, entities={}, keys={}, time={}ms =====",
            domain, entities.size(), keyCount.size(), elapsedTime);
    }
    
    /**
     * Prefix로 자동완성 결과 조회
     * 
     * @param domain 도메인
     * @param prefix 검색 prefix
     * @param limit 결과 개수 제한
     * @return Entity ID 목록 (popularity 높은 순)
     */
    public List<Long> getAutocompleteResults(String domain, String prefix, int limit) {
        String key = buildAutocompleteKey(domain, prefix.substring(0, Math.min(prefix.length(), MAX_PREFIX_LENGTH)));
        
        Set<String> results = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
        
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        
        return results.stream()
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }
    
    /**
     * 엔티티를 자동완성 캐시에 추가
     * 
     * @param domain 도메인
     * @param entity 추가할 엔티티
     */
    public void addToAutocompleteCache(String domain, AutocompleteEntity entity) {
        Set<String> prefixes = generatePrefixes(entity.name());
        
        for (String prefix : prefixes) {
            String key = buildAutocompleteKey(domain, prefix);
            redisTemplate.opsForZSet().add(key, String.valueOf(entity.id()), entity.popularity());
            redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
        }
        
        cacheDetailData(domain, entity.id(), entity.attributes());
        
        log.debug("자동완성 캐시 추가: domain={}, entityId={}, name={}", domain, entity.id(), entity.name());
    }
    
    /**
     * 엔티티를 자동완성 캐시에서 삭제
     * 
     * @param domain 도메인
     * @param entityId Entity ID
     * @param name 엔티티 이름
     */
    public void removeFromAutocompleteCache(String domain, Long entityId, String name) {
        Set<String> prefixes = generatePrefixes(name);
        
        for (String prefix : prefixes) {
            String key = buildAutocompleteKey(domain, prefix);
            redisTemplate.opsForZSet().remove(key, String.valueOf(entityId));
        }
        
        removeDetailData(domain, entityId);
        
        log.debug("자동완성 캐시 삭제: domain={}, entityId={}", domain, entityId);
    }
    
    // ==================== 상세 데이터 캐시 ====================
    
    /**
     * 엔티티 상세 데이터 캐시
     * 
     * @param domain 도메인
     * @param entityId Entity ID
     * @param attributes 속성 맵
     */
    public void cacheDetailData(String domain, Long entityId, Map<String, String> attributes) {
        String key = buildDetailKey(domain, entityId);
        redisTemplate.opsForHash().putAll(key, attributes);
        redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
    }
    
    /**
     * 엔티티 상세 데이터 조회
     * 
     * @param domain 도메인
     * @param entityId Entity ID
     * @return 속성 맵
     */
    public Map<Object, Object> getDetailData(String domain, Long entityId) {
        String key = buildDetailKey(domain, entityId);
        return redisTemplate.opsForHash().entries(key);
    }
    
    /**
     * 여러 엔티티의 상세 데이터 일괄 조회
     * 
     * @param domain 도메인
     * @param entityIds Entity ID 목록
     * @return Entity ID → 속성 맵
     */
    public Map<Long, Map<Object, Object>> getDetailDataBatch(String domain, List<Long> entityIds) {
        Map<Long, Map<Object, Object>> result = new HashMap<>();
        
        for (Long entityId : entityIds) {
            Map<Object, Object> detail = getDetailData(domain, entityId);
            if (!detail.isEmpty()) {
                result.put(entityId, detail);
            }
        }
        
        return result;
    }
    
    /**
     * 엔티티 상세 데이터 삭제
     * 
     * @param domain 도메인
     * @param entityId Entity ID
     */
    public void removeDetailData(String domain, Long entityId) {
        String key = buildDetailKey(domain, entityId);
        redisTemplate.delete(key);
    }
    
    // ==================== 인기 검색어 관리 ====================
    
    /**
     * 검색어 카운트 증가 (인기 검색어 집계)
     * 
     * @param domain 도메인
     * @param keyword 검색어
     */
    public void incrementSearchCount(String domain, String keyword) {
        String key = buildTrendingKey(domain);
        redisTemplate.opsForZSet().incrementScore(key, keyword, 1.0);
        redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
    }
    
    /**
     * 인기 검색어 조회
     * 
     * @param domain 도메인
     * @param limit 결과 개수
     * @return 인기 검색어 목록 (검색 횟수 포함)
     */
    public List<TrendingKeyword> getTrendingKeywords(String domain, int limit) {
        String key = buildTrendingKey(domain);
        Set<ZSetOperations.TypedTuple<String>> results = 
            redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);
        
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        
        return results.stream()
            .map(tuple -> new TrendingKeyword(
                tuple.getValue(),
                tuple.getScore() != null ? tuple.getScore().longValue() : 0L
            ))
            .collect(Collectors.toList());
    }
    
    // ==================== 관련 검색어 추천 ====================
    
    /**
     * 관련 검색어 추가
     * 
     * @param keyword 기준 검색어
     * @param relatedKeywords 관련 검색어 목록
     */
    public void addRelatedKeywords(String keyword, Set<String> relatedKeywords) {
        String key = buildRelatedKey(keyword);
        redisTemplate.opsForSet().add(key, relatedKeywords.toArray(new String[0]));
        redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
    }
    
    /**
     * 관련 검색어 조회
     * 
     * @param keyword 기준 검색어
     * @return 관련 검색어 목록
     */
    public Set<String> getRelatedKeywords(String keyword) {
        String key = buildRelatedKey(keyword);
        return redisTemplate.opsForSet().members(key);
    }
    
    // ==================== 유틸리티 메서드 ====================
    
    /**
     * 이름에서 1-2자 prefix 추출
     * 
     * @param name 이름
     * @return Prefix 집합
     */
    private Set<String> generatePrefixes(String name) {
        Set<String> prefixes = new HashSet<>();
        
        if (name == null || name.isEmpty()) {
            return prefixes;
        }
        
        // 1자 prefix
        if (name.length() >= 1) {
            prefixes.add(name.substring(0, 1));
        }
        
        // 2자 prefix
        if (name.length() >= 2) {
            prefixes.add(name.substring(0, 2));
        }
        
        return prefixes;
    }
    
    /**
     * Redis 키 생성: 자동완성
     */
    private String buildAutocompleteKey(String domain, String prefix) {
        return String.format("autocomplete:%s:%s", domain, prefix);
    }
    
    /**
     * Redis 키 생성: 상세 데이터
     */
    private String buildDetailKey(String domain, Long entityId) {
        return String.format("%s:detail:%d", domain, entityId);
    }
    
    /**
     * Redis 키 생성: 인기 검색어
     */
    private String buildTrendingKey(String domain) {
        return String.format("trending:%s", domain);
    }
    
    /**
     * Redis 키 생성: 관련 검색어
     */
    private String buildRelatedKey(String keyword) {
        return String.format("related:%s", keyword);
    }
    
    /**
     * 도메인 전체 캐시 삭제
     * 
     * @param domain 도메인
     */
    public void clearAllCache(String domain) {
        Set<String> keys = new HashSet<>();
        
        // autocomplete:domain:* 패턴 키
        Set<String> autocompleteKeys = redisTemplate.keys("autocomplete:" + domain + ":*");
        if (autocompleteKeys != null) {
            keys.addAll(autocompleteKeys);
        }
        
        // domain:detail:* 패턴 키
        Set<String> detailKeys = redisTemplate.keys(domain + ":detail:*");
        if (detailKeys != null) {
            keys.addAll(detailKeys);
        }
        
        // trending:domain 키
        keys.add(buildTrendingKey(domain));
        
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("도메인 캐시 전체 삭제: domain={}, keys={}", domain, keys.size());
        }
    }
    
    // ==================== DTO 클래스 ====================
    
    /**
     * 자동완성용 엔티티
     */
    public record AutocompleteEntity(
        Long id,
        String name,
        double popularity,
        Map<String, String> attributes
    ) {}
    
    /**
     * 인기 검색어
     */
    public record TrendingKeyword(
        String keyword,
        Long searchCount
    ) {}
}
