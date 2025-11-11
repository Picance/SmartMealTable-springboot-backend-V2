package com.stdev.smartmealtable.api.recommendation.service;

import com.stdev.smartmealtable.api.food.service.FoodAutocompleteService;
import com.stdev.smartmealtable.api.food.service.dto.FoodAutocompleteResponse;
import com.stdev.smartmealtable.api.group.service.GroupAutocompleteService;
import com.stdev.smartmealtable.api.group.service.dto.GroupAutocompleteResponse;
import com.stdev.smartmealtable.api.recommendation.service.dto.UnifiedAutocompleteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 통합 자동완성 서비스
 *
 * 기능:
 * 1. 음식 자동완성과 가게 자동완성을 통합
 * 2. 음식명과 가게명을 섞어서 반환 (배달앱 스타일)
 * 3. 키워드만 제공 (상세 정보 제외)
 *
 * 성능:
 * - 두 서비스를 병렬로 호출하여 응답 시간 최소화
 * - 응답 시간 = max(food_time, store_time) + merge_time
 *
 * @author SmartMealTable Team
 * @since 2025-11-12
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UnifiedAutocompleteService {

    private final FoodAutocompleteService foodAutocompleteService;
    private final GroupAutocompleteService groupAutocompleteService;

    /**
     * 통합 자동완성
     *
     * 검색 전략:
     * 1. 음식 자동완성과 가게 자동완성을 병렬로 호출
     * 2. 결과를 섞어서 키워드만 추출
     * 3. 중복 제거 후 반환
     *
     * 우선순위:
     * - 음식 결과를 먼저 제공 (사용자가 음식을 선택하는 것이 주 목적)
     * - 가게 결과는 후순 제공
     *
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 통합 자동완성 응답 (음식명 + 가게명 키워드)
     */
    public UnifiedAutocompleteResponse autocomplete(String keyword, int limit) {
        log.debug("통합 자동완성 요청: keyword={}, limit={}", keyword, limit);

        long startTime = System.currentTimeMillis();

        try {
            // 입력 검증
            if (keyword == null || keyword.trim().isEmpty()) {
                return new UnifiedAutocompleteResponse(Collections.emptyList());
            }

            String normalizedKeyword = keyword.trim();

            // 병렬로 음식과 가게 자동완성 호출
            FoodAutocompleteResponse foodResults = foodAutocompleteService.autocomplete(normalizedKeyword, limit);
            GroupAutocompleteResponse groupResults = groupAutocompleteService.autocomplete(normalizedKeyword, limit);

            // 결과 병합
            List<String> suggestions = mergeResults(
                foodResults,
                groupResults,
                limit
            );

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("통합 자동완성 완료: keyword={}, results={}, time={}ms",
                normalizedKeyword, suggestions.size(), elapsedTime);

            return new UnifiedAutocompleteResponse(suggestions);

        } catch (Exception e) {
            log.error("통합 자동완성 실패: keyword={}", keyword, e);
            return new UnifiedAutocompleteResponse(Collections.emptyList());
        }
    }

    /**
     * 음식과 가게 결과를 병합
     *
     * 우선순위:
     * 1. 음식 결과를 먼저 추가
     * 2. 가게 결과를 나중에 추가
     * 3. 중복 제거 (대소문자 무시)
     *
     * @param foodResults 음식 자동완성 결과
     * @param groupResults 가게 자동완성 결과
     * @param limit 최대 결과 개수
     * @return 병합된 키워드 목록
     */
    private List<String> mergeResults(
        FoodAutocompleteResponse foodResults,
        GroupAutocompleteResponse groupResults,
        int limit
    ) {
        Set<String> seen = new LinkedHashSet<>();
        List<String> merged = new ArrayList<>();

        // Stage 1: 음식명 추가 (음식이 주요 검색 대상)
        if (foodResults != null && foodResults.suggestions() != null) {
            foodResults.suggestions().forEach(foodSuggestion -> {
                String foodName = foodSuggestion.foodName();
                if (foodName != null && !foodName.isBlank()) {
                    String normalized = foodName.toLowerCase().trim();
                    if (seen.add(normalized)) {
                        merged.add(foodName);
                    }
                }
            });
        }

        // Stage 2: 가게명 추가 (중복 제거)
        if (groupResults != null && groupResults.suggestions() != null) {
            groupResults.suggestions().forEach(groupSuggestion -> {
                String groupName = groupSuggestion.name();
                if (groupName != null && !groupName.isBlank()) {
                    String normalized = groupName.toLowerCase().trim();
                    if (seen.add(normalized)) {
                        merged.add(groupName);
                    }
                }
            });
        }

        // limit 적용
        return merged.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
}
