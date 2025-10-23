package com.stdev.smartmealtable.api.food.controller;

import com.stdev.smartmealtable.api.store.dto.GetFoodDetailResponse;
import com.stdev.smartmealtable.api.store.service.StoreService;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 음식(메뉴) 관리 API Controller
 * - 음식 상세 조회
 */
@RestController
@RequestMapping("/api/v1/foods")
@RequiredArgsConstructor
@Validated
@Slf4j
public class FoodController {
    
    private final StoreService storeService;
    
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
}
