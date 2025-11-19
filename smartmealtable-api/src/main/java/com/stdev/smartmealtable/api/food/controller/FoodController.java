package com.stdev.smartmealtable.api.food.controller;

import com.stdev.smartmealtable.api.common.auth.OptionalAuthenticatedUserProvider;
import com.stdev.smartmealtable.api.food.service.FoodAutocompleteService;
import com.stdev.smartmealtable.api.food.service.dto.FoodAutocompleteResponse;
import com.stdev.smartmealtable.api.food.service.dto.FoodTrendingKeywordsResponse;
import com.stdev.smartmealtable.api.store.dto.GetFoodDetailResponse;
import com.stdev.smartmealtable.api.store.service.StoreService;
import com.stdev.smartmealtable.api.recommendation.service.AutocompleteSearchEventService;
import com.stdev.smartmealtable.api.recommendation.service.AutocompleteSearchEventService.AutocompleteSearchEventCommand;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 음식(메뉴) 관리 API Controller
 * - 음식 상세 조회
 * - 음식 자동완성 검색
 */
@RestController
@RequestMapping("/api/v1/foods")
@RequiredArgsConstructor
@Validated
@Slf4j
public class FoodController {
    
    private final StoreService storeService;
    private final FoodAutocompleteService foodAutocompleteService;
    private final AutocompleteSearchEventService autocompleteSearchEventService;
    private final OptionalAuthenticatedUserProvider optionalAuthenticatedUserProvider;
    
    /**
     * 메뉴 상세 조회
     * GET /api/v1/foods/{foodId}
     *
     * @param user 인증된 사용자
     * @param foodId 메뉴 ID
     * @return 메뉴 상세 정보 (가게 정보, 예산 비교 포함)
     */
    @GetMapping("/{foodId}")
    public ApiResponse<GetFoodDetailResponse> getFoodDetail(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long foodId
    ) {
        log.info("메뉴 상세 조회 API 호출 - foodId: {}, memberId: {}", foodId, user.memberId());
        
        GetFoodDetailResponse response = storeService.getFoodDetail(user.memberId(), foodId);
        
        return ApiResponse.success(response);
    }
    
    // ==================== 검색 기능 강화 API ====================
    
    /**
     * 음식 자동완성 (검색 기능 강화)
     * GET /api/v1/foods/autocomplete?keyword=떡볶&limit=10
     *
     * @param keyword 검색 키워드 (필수, 1-50자)
     * @param limit 결과 개수 제한 (기본값: 10, 최대: 20)
     * @return 자동완성 제안 목록
     */
    @GetMapping("/autocomplete")
    public ApiResponse<FoodAutocompleteResponse> autocomplete(
            @RequestParam @Size(min = 1, max = 50, message = "검색 키워드는 1-50자 이내여야 합니다.") String keyword,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit,
            HttpServletRequest request
    ) {
        log.info("음식 자동완성 API 호출 - keyword: {}, limit: {}", keyword, limit);

        FoodAutocompleteResponse response = foodAutocompleteService.autocomplete(keyword, limit);
        publishSearchEvent(keyword, request);

        return ApiResponse.success(response);
    }
    
    /**
     * 음식 인기 검색어 조회
     * GET /api/v1/foods/trending?limit=10
     *
     * @param limit 결과 개수 (기본값: 10, 최대: 20)
     * @return 인기 검색어 목록
     */
    @GetMapping("/trending")
    public ApiResponse<FoodTrendingKeywordsResponse> getTrendingKeywords(
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit
    ) {
        log.info("음식 인기 검색어 조회 API 호출 - limit: {}", limit);
        
        FoodTrendingKeywordsResponse response = foodAutocompleteService.getTrendingKeywords(limit);
        
        return ApiResponse.success(response);
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
}
