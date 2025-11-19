package com.stdev.smartmealtable.api.recommendation.controller;

import com.stdev.smartmealtable.api.common.auth.OptionalAuthenticatedUserProvider;
import com.stdev.smartmealtable.api.recommendation.service.AutocompleteSearchEventService;
import com.stdev.smartmealtable.api.recommendation.service.AutocompleteSearchEventService.AutocompleteSearchEventCommand;
import com.stdev.smartmealtable.api.recommendation.service.UnifiedAutocompleteService;
import com.stdev.smartmealtable.api.recommendation.service.dto.AutocompleteClickRequest;
import com.stdev.smartmealtable.api.recommendation.service.dto.UnifiedAutocompleteResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 통합 자동완성 API Controller
 *
 * 기능:
 * - 음식과 가게명을 통합해서 자동완성 제공 (배달앱 스타일)
 * - 키워드만 반환 (상세 정보 제외)
 *
 * @author SmartMealTable Team
 * @since 2025-11-12
 */
@RestController
@RequestMapping("/api/v1/autocomplete")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AutocompleteController {

    private final UnifiedAutocompleteService unifiedAutocompleteService;
    private final AutocompleteSearchEventService autocompleteSearchEventService;
    private final OptionalAuthenticatedUserProvider optionalAuthenticatedUserProvider;

    /**
     * 통합 자동완성 (음식 + 가게)
     * GET /api/v1/autocomplete?keyword=김치&limit=10&storeShortcutsLimit=5
     *
     * 응답 예시:
     * {
     *   "result": "SUCCESS",
     *   "data": {
     *     "suggestions": [
     *       "김치찜",
     *       "김치전",
     *       "김치나라",
     *       "김치국",
     *       "한식당"
     *     ],
     *     "storeShortcuts": [
     *       {
     *         "storeId": 101,
     *         "name": "김치나라",
     *         "imageUrl": "https://...",
     *         "isOpen": true
     *       }
     *     ]
     *   }
     * }
     *
     * 검색 순서:
     * 1. 음식명 (음식이 주요 검색 대상)
     * 2. 가게명 (부가 정보)
     *
     * @param keyword 검색 키워드 (필수, 1-50자)
     * @param limit 결과 개수 제한 (기본값: 10, 최대: 20)
     * @param storeShortcutsLimit 가게 바로가기 개수 제한 (기본값: 10, 최소 1, 최대 20)
     * @return 자동완성 키워드 목록 (음식명 + 가게명) 및 가게 바로가기 목록
     */
    @GetMapping
    public ApiResponse<UnifiedAutocompleteResponse> autocomplete(
            @RequestParam @Size(min = 1, max = 50, message = "검색 키워드는 1-50자 이내여야 합니다.") String keyword,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int storeShortcutsLimit,
            HttpServletRequest request
    ) {
        log.info("통합 자동완성 API 호출 - keyword: {}, limit: {}, storeShortcutsLimit: {}", keyword, limit, storeShortcutsLimit);

        UnifiedAutocompleteResponse response = unifiedAutocompleteService.autocomplete(keyword, limit, storeShortcutsLimit);
        publishSearchEvent(keyword, request);

        return ApiResponse.success(response);
    }

    /**
     * 자동완성 클릭 이벤트 기록
     */
    @PostMapping("/events")
    public ApiResponse<Void> recordAutocompleteEvent(
            @Valid @RequestBody AutocompleteClickRequest request,
            HttpServletRequest httpServletRequest
    ) {
        Long memberId = optionalAuthenticatedUserProvider.extractMemberId(httpServletRequest).orElse(null);
        AutocompleteSearchEventCommand command = AutocompleteSearchEventCommand.builder()
                .rawKeyword(request.keyword())
                .memberId(memberId)
                .clickedFoodId(request.foodId())
                .latitude(toBigDecimal(request.latitude()))
                .longitude(toBigDecimal(request.longitude()))
                .build();
        autocompleteSearchEventService.logSearchEvent(command);
        return ApiResponse.success();
    }

    private void publishSearchEvent(String keyword, HttpServletRequest request) {
        Long memberId = optionalAuthenticatedUserProvider.extractMemberId(request).orElse(null);
        AutocompleteSearchEventCommand command = AutocompleteSearchEventCommand.builder()
                .rawKeyword(keyword)
                .memberId(memberId)
                .clickedFoodId(null)
                .latitude(null)
                .longitude(null)
                .build();
        autocompleteSearchEventService.logSearchEvent(command);
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
