package com.stdev.smartmealtable.api.group.service;

import com.stdev.smartmealtable.api.group.service.dto.GroupAutocompleteResponse;
import com.stdev.smartmealtable.api.group.service.dto.GroupAutocompleteResponse.GroupSuggestion;
import com.stdev.smartmealtable.api.group.service.dto.TrendingKeywordsResponse;
import com.stdev.smartmealtable.api.group.service.dto.TrendingKeywordsResponse.TrendingKeyword;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.storage.cache.ChosungIndexBuilder;
import com.stdev.smartmealtable.storage.cache.SearchCacheService;
import com.stdev.smartmealtable.support.search.korean.KoreanSearchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 그룹 자동완성 서비스
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
 * @since 2025-11-09
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupAutocompleteService {
    
    private final GroupRepository groupRepository;
    private final SearchCacheService searchCacheService;
    private final ChosungIndexBuilder chosungIndexBuilder;
    
    private static final String DOMAIN = "group";
    private static final int MAX_TYPO_DISTANCE = 2;
    private static final int MIN_RESULTS_FOR_TYPO = 5;
    
    /**
     * 그룹 자동완성
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
    public GroupAutocompleteResponse autocomplete(String keyword, int limit) {
        log.debug("그룹 자동완성 요청: keyword={}, limit={}", keyword, limit);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 입력 검증
            if (keyword == null || keyword.trim().isEmpty()) {
                return new GroupAutocompleteResponse(Collections.emptyList());
            }
            
            String normalizedKeyword = keyword.trim();
            
            // 2. 검색 횟수 증가 (인기 검색어 집계)
            searchCacheService.incrementSearchCount(DOMAIN, normalizedKeyword);
            
            // 3. 다단계 검색 전략 실행
            List<Group> results = performMultiStageSearch(normalizedKeyword, limit);
            
            // 4. DTO 변환
            List<GroupSuggestion> suggestions = results.stream()
                .limit(limit)
                .map(this::toSuggestion)
                .collect(Collectors.toList());
            
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("그룹 자동완성 완료: keyword={}, results={}, time={}ms", 
                normalizedKeyword, suggestions.size(), elapsedTime);
            
            return new GroupAutocompleteResponse(suggestions);
            
        } catch (Exception e) {
            log.error("그룹 자동완성 실패: keyword={}", keyword, e);
            // Fallback: DB 직접 검색
            return fallbackSearch(keyword, limit);
        }
    }
    
    /**
     * 다단계 검색 전략
     *
     * 핵심: 캐시에서 popularity 순 정렬 데이터를 먼저 조회하고, 결과 개수와 상관없이 우선 반환
     * Stage 1: Redis 캐시에서 Prefix 검색 (popularity 순 정렬) - 가장 높은 우선순위
     * Stage 2: 초성 검색 (캐시)
     * Stage 3: DB 기반 다단계 검색 (Prefix → Contains → Edit Distance)
     *
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 검색 결과 (limit만큼 제한됨)
     */
    private List<Group> performMultiStageSearch(String keyword, int limit) {
        // Stage 1: Redis 캐시에서 Prefix 검색 (popularity 순으로 이미 정렬됨) - 최고 우선순위
        try {
            List<Long> cachedIds = searchCacheService.getAutocompleteResults(DOMAIN, keyword, limit);

            if (!cachedIds.isEmpty()) {
                log.debug("캐시에서 결과 조회: keyword={}, results={}", keyword, cachedIds.size());
                List<Group> results = fetchGroups(cachedIds);

                // 캐시 결과는 이미 popularity 순으로 정렬되어 있으므로, 결과가 있으면 바로 반환
                // (limit보다 적을 수 있지만, 캐시된 데이터가 최고 우선순위)
                if (!results.isEmpty()) {
                    log.debug("캐시 결과 반환: {}", results.size());
                    return results;
                }
            }
        } catch (Exception e) {
            log.warn("캐시 검색 실패", e);
        }

        // Stage 2: 초성 검색 시도
        if (KoreanSearchUtil.isChosung(keyword)) {
            try {
                Set<String> chosungResults = chosungIndexBuilder.findIdsByChosung(DOMAIN, keyword);
                if (!chosungResults.isEmpty()) {
                    List<Long> ids = chosungResults.stream()
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                    List<Group> groups = fetchGroups(ids);
                    if (!groups.isEmpty()) {
                        log.debug("초성 검색으로 결과 반환: {}", groups.size());
                        return groups;
                    }
                }
            } catch (Exception e) {
                log.warn("초성 검색 실패", e);
            }
        }

        // Stage 3: DB 기반 다단계 검색 (정렬 로직 포함)
        try {
            List<Group> results = searchWithTypoTolerance(keyword, limit);

            if (!results.isEmpty()) {
                log.debug("DB 기반 검색 결과 반환: keyword={}, results={}", keyword, results.size());
                return results;
            }
        } catch (Exception e) {
            log.warn("DB 기반 검색 실패", e);
        }

        // 최후의 fallback: fallbackSearch
        return fallbackSearch(keyword, limit).suggestions().stream()
            .map(suggestion -> {
                // TODO: GroupSuggestion → Group 변환 로직 개선 필요
                List<Group> groups = groupRepository.findAllByIdIn(List.of(suggestion.groupId()));
                return groups.isEmpty() ? null : groups.get(0);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * 오타 허용 + 부분 매칭 검색
     *
     * 검색 전략:
     * 1. Prefix 검색 (정확한 매칭)
     * 2. Contains 검색 (부분 매칭, 이름 길이순)
     * 3. Edit Distance 검색 (오타 허용)
     *
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 검색 결과
     */
    private List<Group> searchWithTypoTolerance(String keyword, int limit) {
        Set<Long> resultIds = new LinkedHashSet<>();
        List<Group> results = new ArrayList<>();

        // Stage 1: Prefix 검색 (정확한 매칭 우선)
        try {
            List<Group> prefixResults = groupRepository.findByNameStartsWith(keyword);
            prefixResults.forEach(group -> {
                if (resultIds.add(group.getGroupId())) {
                    results.add(group);
                }
            });

            if (results.size() >= limit) {
                log.debug("Prefix 검색으로 충분한 결과: {}", results.size());
                return results.stream().limit(limit).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Prefix 검색 실패", e);
        }

        // Stage 2: Contains 검색 (부분 매칭, 이름 길이순 - UI/UX 개선)
        if (results.size() < limit) {
            try {
                List<Group> containsResults = groupRepository.findByNameContainsOrderByLength(keyword);

                containsResults.forEach(group -> {
                    if (resultIds.add(group.getGroupId())) {
                        results.add(group);
                    }
                });

                if (results.size() >= limit) {
                    log.debug("Contains 검색으로 충분한 결과: {}", results.size());
                    return results.stream().limit(limit).collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("Contains 검색 실패", e);
            }
        }

        // Stage 3: Edit Distance 기반 검색 (오타 허용)
        if (results.size() < limit && keyword.length() >= 2) {
            try {
                String prefix = keyword.substring(0, Math.min(2, keyword.length()));
                List<Group> candidates = groupRepository.findByNameStartsWith(prefix);

                candidates.stream()
                    .filter(group -> {
                        int distance = KoreanSearchUtil.calculateEditDistance(keyword, group.getName());
                        return distance <= MAX_TYPO_DISTANCE && !resultIds.contains(group.getGroupId());
                    })
                    .sorted(Comparator.comparingInt(group ->
                        KoreanSearchUtil.calculateEditDistance(keyword, group.getName())
                    ))
                    .limit(limit - results.size())
                    .forEach(group -> {
                        if (resultIds.add(group.getGroupId())) {
                            results.add(group);
                        }
                    });

                log.debug("Edit Distance 검색 추가 결과: {}", candidates.size());
            } catch (Exception e) {
                log.warn("Edit Distance 검색 실패", e);
            }
        }

        return results;
    }
    
    /**
     * Group ID 목록으로 Group 엔티티 조회 (입력 순서 유지)
     *
     * @param groupIds Group ID 목록 (원하는 순서대로)
     * @return Group 엔티티 목록 (입력 순서 유지)
     */
    private List<Group> fetchGroups(List<Long> groupIds) {
        if (groupIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            log.debug("fetchGroups 호출: groupIds={}", groupIds);

            // 1. 먼저 모든 ID를 DB에서 조회 (캐시 활용)
            List<Group> dbGroups = groupRepository.findAllByIdIn(groupIds);
            log.debug("DB에서 조회한 그룹: {}", dbGroups.stream().map(g -> g.getGroupId() + ":" + g.getName()).collect(Collectors.toList()));

            // DB에서 조회한 데이터를 캐시에 저장
            cacheGroups(dbGroups);

            // 2. 입력된 ID 순서대로 결과 정렬
            Map<Long, Group> groupMap = dbGroups.stream()
                .collect(Collectors.toMap(Group::getGroupId, g -> g));

            List<Group> result = groupIds.stream()
                .map(groupMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            log.debug("최종 반환 결과: {}", result.stream().map(g -> g.getGroupId() + ":" + g.getName()).collect(Collectors.toList()));

            return result;

        } catch (Exception e) {
            log.error("Group 조회 실패, DB fallback", e);
            return groupRepository.findAllByIdIn(groupIds);
        }
    }
    
    /**
     * Group 목록을 Redis 캐시에 저장
     * 
     * @param groups Group 목록
     */
    private void cacheGroups(List<Group> groups) {
        for (Group group : groups) {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("name", group.getName());
            attributes.put("type", group.getType().name());
            attributes.put("address", group.getAddress() != null ? group.getAddress().getFullAddress() : "");

            searchCacheService.cacheDetailData(DOMAIN, group.getGroupId(), attributes);
        }
    }
    
    /**
     * Fallback 검색 (Redis 장애 시)
     *
     * 검색 전략:
     * 1. 정확한 매칭 (Exact match): 이름이 정확히 일치 또는 대부분 일치
     * 2. 부분 매칭 (Contains match): 이름 길이 짧은 순으로 정렬
     *
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 자동완성 응답
     */
    private GroupAutocompleteResponse fallbackSearch(String keyword, int limit) {
        log.warn("Fallback 검색 실행: keyword={}", keyword);

        try {
            List<Group> groups;

            // 초성 검색 여부 확인
            if (KoreanSearchUtil.isChosung(keyword)) {
                // 초성 검색: DB에서 모든 그룹 조회 후 필터링
                groups = groupRepository.findByNameStartsWith(keyword.substring(0, 1));
                groups = groups.stream()
                    .filter(group -> KoreanSearchUtil.matchesChosung(keyword, group.getName()))
                    .sorted(Comparator.comparingInt(group -> group.getName().length())) // 이름 길이순 정렬
                    .limit(limit)
                    .collect(Collectors.toList());
            } else {
                // 일반 검색: 정확한 매칭 → 부분 매칭 순서
                groups = new ArrayList<>();

                // Stage 1: Prefix 검색 (정확한 매칭 우선)
                List<Group> prefixResults = groupRepository.findByNameStartsWith(keyword);
                groups.addAll(prefixResults);

                if (groups.size() < limit) {
                    // Stage 2: Contains 검색 (이름 길이순으로 정렬 - UI/UX 개선)
                    List<Group> containsResults = groupRepository.findByNameContainsOrderByLength(keyword);

                    // 중복 제거 후 추가
                    Set<Long> existingIds = groups.stream()
                        .map(Group::getGroupId)
                        .collect(Collectors.toSet());

                    containsResults.stream()
                        .filter(group -> !existingIds.contains(group.getGroupId()))
                        .forEach(groups::add);
                }
            }

            List<GroupSuggestion> suggestions = groups.stream()
                .limit(limit)
                .map(this::toSuggestion)
                .collect(Collectors.toList());

            log.debug("Fallback 검색 완료: keyword={}, results={}", keyword, suggestions.size());
            return new GroupAutocompleteResponse(suggestions);

        } catch (Exception e) {
            log.error("Fallback 검색 실패", e);
            return new GroupAutocompleteResponse(Collections.emptyList());
        }
    }
    
    /**
     * 두 결과 목록을 병합 (중복 제거)
     * 
     * @param list1 첫 번째 목록
     * @param list2 두 번째 목록
     * @param limit 최대 개수
     * @return 병합된 목록
     */
    private List<Group> mergeResults(List<Group> list1, List<Group> list2, int limit) {
        Set<Long> ids = new LinkedHashSet<>();
        List<Group> merged = new ArrayList<>();
        
        for (Group group : list1) {
            if (ids.add(group.getGroupId())) {
                merged.add(group);
            }
        }
        
        for (Group group : list2) {
            if (ids.add(group.getGroupId())) {
                merged.add(group);
            }
            if (merged.size() >= limit) {
                break;
            }
        }
        
        return merged;
    }
    
    /**
     * 인기 검색어 조회
     * 
     * @param limit 결과 개수
     * @return 인기 검색어 목록
     */
    public TrendingKeywordsResponse getTrendingKeywords(int limit) {
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
            return new TrendingKeywordsResponse(keywords);
            
        } catch (Exception e) {
            log.error("인기 검색어 조회 실패", e);
            return new TrendingKeywordsResponse(Collections.emptyList());
        }
    }
    
    /**
     * Group → GroupSuggestion 변환
     * 
     * @param group Group 엔티티
     * @return GroupSuggestion DTO
     */
    private GroupSuggestion toSuggestion(Group group) {
        return new GroupSuggestion(
            group.getGroupId(),
            group.getName(),
            group.getType(),
            group.getAddress() != null ? group.getAddress().getFullAddress() : null
        );
    }
}
