package com.stdev.smartmealtable.support.search.cache;

import com.stdev.smartmealtable.support.search.korean.KoreanSearchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 초성 역인덱스 빌더
 * 
 * 목적:
 * - 초성 검색 최적화를 위한 역인덱스 구축
 * - Redis Set을 사용하여 초성 → Entity ID 매핑
 * 
 * Redis 구조:
 * - Key: chosung_index:{domain}:{chosung}
 * - Type: Set
 * - Value: Entity ID 목록
 * - TTL: 24시간
 * 
 * 예시:
 * - chosung_index:group:ㅅㅇㄷㅎㄱ → {1, 2, 3} (서울대학교, 서울교대, 서울산업대)
 * 
 * @author SmartMealTable Team
 * @since 2025-11-09
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChosungIndexBuilder {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final long TTL_HOURS = 24;
    
    /**
     * 도메인 전체의 초성 역인덱스 구축
     * 
     * @param domain 도메인 이름 (예: "group", "recommendation")
     * @param entities 엔티티 목록 (ID와 이름 포함)
     */
    public void buildChosungIndex(String domain, List<SearchableEntity> entities) {
        log.info("===== 초성 역인덱스 구축 시작: domain={}, entities={} =====", domain, entities.size());
        
        long startTime = System.currentTimeMillis();
        int indexCount = 0;
        
        for (SearchableEntity entity : entities) {
            String chosung = KoreanSearchUtil.extractChosung(entity.name());
            
            if (chosung.isEmpty()) {
                continue; // 한글이 없는 경우 skip
            }
            
            // Redis Set에 추가
            String key = buildChosungKey(domain, chosung);
            redisTemplate.opsForSet().add(key, String.valueOf(entity.id()));
            
            // TTL 설정 (첫 추가 시에만)
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
            }
            
            indexCount++;
        }
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        
        log.info("===== 초성 역인덱스 구축 완료: domain={}, entities={}, indexes={}, time={}ms =====",
            domain, entities.size(), indexCount, elapsedTime);
    }
    
    /**
     * 특정 초성으로 Entity ID 목록 조회
     * 
     * @param domain 도메인 이름
     * @param chosung 초성 문자열
     * @return Entity ID 목록
     */
    public Set<String> findIdsByChosung(String domain, String chosung) {
        String key = buildChosungKey(domain, chosung);
        return redisTemplate.opsForSet().members(key);
    }
    
    /**
     * 특정 Entity의 초성 인덱스 추가
     * 
     * @param domain 도메인 이름
     * @param entity 추가할 엔티티
     */
    public void addToChosungIndex(String domain, SearchableEntity entity) {
        String chosung = KoreanSearchUtil.extractChosung(entity.name());
        
        if (chosung.isEmpty()) {
            return;
        }
        
        String key = buildChosungKey(domain, chosung);
        redisTemplate.opsForSet().add(key, String.valueOf(entity.id()));
        redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
        
        log.debug("초성 인덱스 추가: domain={}, chosung={}, entityId={}", domain, chosung, entity.id());
    }
    
    /**
     * 특정 Entity의 초성 인덱스 삭제
     * 
     * @param domain 도메인 이름
     * @param entity 삭제할 엔티티
     */
    public void removeFromChosungIndex(String domain, SearchableEntity entity) {
        String chosung = KoreanSearchUtil.extractChosung(entity.name());
        
        if (chosung.isEmpty()) {
            return;
        }
        
        String key = buildChosungKey(domain, chosung);
        redisTemplate.opsForSet().remove(key, String.valueOf(entity.id()));
        
        log.debug("초성 인덱스 삭제: domain={}, chosung={}, entityId={}", domain, chosung, entity.id());
    }
    
    /**
     * 도메인 전체의 초성 인덱스 삭제
     * 
     * @param domain 도메인 이름
     */
    public void clearChosungIndex(String domain) {
        String pattern = buildChosungKey(domain, "*");
        Set<String> keys = redisTemplate.keys(pattern);
        
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("초성 인덱스 전체 삭제: domain={}, keys={}", domain, keys.size());
        }
    }
    
    /**
     * Redis 키 생성
     * 
     * @param domain 도메인 이름
     * @param chosung 초성 문자열
     * @return Redis 키
     */
    private String buildChosungKey(String domain, String chosung) {
        return String.format("chosung_index:%s:%s", domain, chosung);
    }
    
    /**
     * 검색 가능한 엔티티 인터페이스
     */
    public record SearchableEntity(Long id, String name) {}
}
