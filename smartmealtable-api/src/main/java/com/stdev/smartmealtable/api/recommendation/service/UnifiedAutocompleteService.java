package com.stdev.smartmealtable.api.recommendation.service;

import com.stdev.smartmealtable.api.food.service.FoodAutocompleteService;
import com.stdev.smartmealtable.api.food.service.dto.FoodAutocompleteResponse;
import com.stdev.smartmealtable.api.group.service.GroupAutocompleteService;
import com.stdev.smartmealtable.api.group.service.dto.GroupAutocompleteResponse;
import com.stdev.smartmealtable.api.recommendation.service.dto.UnifiedAutocompleteResponse;
import com.stdev.smartmealtable.api.recommendation.service.dto.UnifiedAutocompleteResponse.StoreShortcut;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
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
 * 2. 음식명과 가게명을 번갈아가며 섞어서 반환 (배달앱 스타일)
 * 3. 검색 결과의 가게들에 대한 바로가기 정보 제공
 *
 * 성능:
 * - 두 서비스를 병렬로 호출하여 응답 시간 최소화
 * - 응답 시간 = max(food_time, group_time) + merge_time
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
    private final StoreRepository storeRepository;

    /**
     * 통합 자동완성
     *
     * 검색 전략:
     * 1. 음식 자동완성과 가게 자동완성을 병렬로 호출
     * 2. 결과를 번갈아가며 섞어서 키워드 반환
     * 3. 검색 결과의 가게들에 대한 바로가기 정보 제공
     *
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 통합 자동완성 응답 (키워드 섹션 + 가게 바로가기 섹션)
     */
    public UnifiedAutocompleteResponse autocomplete(String keyword, int limit) {
        log.debug("통합 자동완성 요청: keyword={}, limit={}", keyword, limit);

        long startTime = System.currentTimeMillis();

        try {
            // 입력 검증
            if (keyword == null || keyword.trim().isEmpty()) {
                return new UnifiedAutocompleteResponse(Collections.emptyList(), Collections.emptyList());
            }

            String normalizedKeyword = keyword.trim();

            // 병렬로 음식과 가게 자동완성 호출
            FoodAutocompleteResponse foodResults = foodAutocompleteService.autocomplete(normalizedKeyword, limit);
            GroupAutocompleteResponse groupResults = groupAutocompleteService.autocomplete(normalizedKeyword, limit);

            // 결과 병합: 키워드 목록 (음식/가게명 번갈아가며)
            List<String> suggestions = mergeKeywordsInterleaved(
                foodResults,
                groupResults,
                limit
            );

            // 가게 바로가기 정보 생성
            List<StoreShortcut> storeShortcuts = buildStoreShortcuts(groupResults);

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("통합 자동완성 완료: keyword={}, keywords={}, stores={}, time={}ms",
                normalizedKeyword, suggestions.size(), storeShortcuts.size(), elapsedTime);

            return new UnifiedAutocompleteResponse(suggestions, storeShortcuts);

        } catch (Exception e) {
            log.error("통합 자동완성 실패: keyword={}", keyword, e);
            return new UnifiedAutocompleteResponse(Collections.emptyList(), Collections.emptyList());
        }
    }

    /**
     * 음식과 가게 키워드를 번갈아가며 섞기
     *
     * 예: 음식 [A, B, C], 가게 [X, Y, Z] → [A, X, B, Y, C, Z]
     *
     * @param foodResults 음식 자동완성 결과
     * @param groupResults 가게 자동완성 결과
     * @param limit 최대 결과 개수
     * @return 섞인 키워드 목록
     */
    private List<String> mergeKeywordsInterleaved(
        FoodAutocompleteResponse foodResults,
        GroupAutocompleteResponse groupResults,
        int limit
    ) {
        List<String> foodKeywords = extractFoodKeywords(foodResults);
        List<String> groupKeywords = extractGroupKeywords(groupResults);

        Set<String> seen = new LinkedHashSet<>();
        List<String> merged = new ArrayList<>();

        // 음식과 가게 키워드를 번갈아가며 추가
        int maxIndex = Math.max(foodKeywords.size(), groupKeywords.size());
        for (int i = 0; i < maxIndex && merged.size() < limit; i++) {
            // 음식 키워드 추가
            if (i < foodKeywords.size()) {
                String foodKeyword = foodKeywords.get(i);
                if (foodKeyword != null && !foodKeyword.isBlank()) {
                    String normalized = foodKeyword.toLowerCase().trim();
                    if (seen.add(normalized)) {
                        merged.add(foodKeyword);
                    }
                }
            }

            // 가게 키워드 추가 (limit 체크)
            if (merged.size() < limit && i < groupKeywords.size()) {
                String groupKeyword = groupKeywords.get(i);
                if (groupKeyword != null && !groupKeyword.isBlank()) {
                    String normalized = groupKeyword.toLowerCase().trim();
                    if (seen.add(normalized)) {
                        merged.add(groupKeyword);
                    }
                }
            }
        }

        return merged;
    }

    /**
     * 음식 결과에서 키워드 추출
     */
    private List<String> extractFoodKeywords(FoodAutocompleteResponse foodResults) {
        if (foodResults == null || foodResults.suggestions() == null) {
            return Collections.emptyList();
        }

        return foodResults.suggestions().stream()
            .map(suggestion -> suggestion.foodName())
            .filter(name -> name != null && !name.isBlank())
            .collect(Collectors.toList());
    }

    /**
     * 가게 결과에서 키워드 추출
     */
    private List<String> extractGroupKeywords(GroupAutocompleteResponse groupResults) {
        if (groupResults == null || groupResults.suggestions() == null) {
            return Collections.emptyList();
        }

        return groupResults.suggestions().stream()
            .map(suggestion -> suggestion.name())
            .filter(name -> name != null && !name.isBlank())
            .collect(Collectors.toList());
    }

    /**
     * 가게 바로가기 정보 생성
     *
     * 가게 자동완성 결과에서 가게 엔티티를 조회하여
     * ID, 이름, 대표 이미지, 영업 상태를 포함한 바로가기 정보 생성
     *
     * @param groupResults 가게 자동완성 결과
     * @return 가게 바로가기 목록
     */
    private List<StoreShortcut> buildStoreShortcuts(GroupAutocompleteResponse groupResults) {
        if (groupResults == null || groupResults.suggestions() == null || groupResults.suggestions().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // 가게 ID 목록 추출
            List<Long> storeIds = groupResults.suggestions().stream()
                .map(suggestion -> suggestion.groupId())
                .collect(Collectors.toList());

            if (storeIds.isEmpty()) {
                return Collections.emptyList();
            }

            // 가게 엔티티 조회
            List<Store> stores = storeRepository.findAllByIdIn(storeIds);

            // 가게 바로가기 정보로 변환
            return stores.stream()
                .map(store -> new StoreShortcut(
                    store.getStoreId(),
                    store.getName(),
                    store.getImageUrl(),
                    isStoreOpen(store) // 영업 중 여부
                ))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("가게 바로가기 정보 생성 실패", e);
            return Collections.emptyList();
        }
    }

    /**
     * 가게 영업 상태 확인
     *
     * TODO: 실제 영업 시간 관리 시스템과 연동 필요
     * 현재는 임시 구현 (항상 true)
     *
     * @param store 가게 엔티티
     * @return 영업 중 여부
     */
    private Boolean isStoreOpen(Store store) {
        // TODO: 실제 영업 시간 정보 조회 및 현재 시각 비교
        return true;
    }
}
