package com.stdev.smartmealtable.admin.food.controller;

import com.stdev.smartmealtable.admin.food.controller.dto.*;
import com.stdev.smartmealtable.admin.food.service.FoodApplicationService;
import com.stdev.smartmealtable.admin.food.service.dto.*;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 음식 관리 Controller (ADMIN)
 */
@RestController
@RequestMapping("/api/v1/admin/stores/{storeId}/foods")
@RequiredArgsConstructor
@Slf4j
@Validated
public class FoodController {

    private final FoodApplicationService foodApplicationService;

    /**
     * 음식 목록 조회 (페이징, 필터링)
     * 
     * @param storeId 가게 ID (필수)
     * @param categoryId 카테고리 ID (선택)
     * @param name 음식 이름 (부분 일치, 선택)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 20, 최대: 100)
     * @return 페이징된 음식 목록
     */
    @GetMapping
    public ApiResponse<FoodListResponse> getFoods(
            @PathVariable @Positive Long storeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        log.info("[ADMIN] GET /api/v1/admin/stores/{}/foods - categoryId: {}, name: {}, page: {}, size: {}", 
                storeId, categoryId, name, page, size);
        
        FoodListServiceRequest serviceRequest = new FoodListServiceRequest(
                categoryId, storeId, name, page, size
        );
        
        FoodListServiceResponse serviceResponse = foodApplicationService.getFoods(serviceRequest);
        FoodListResponse response = FoodListResponse.from(serviceResponse);
        
        return ApiResponse.success(response);
    }

    /**
     * 음식 상세 조회
     * 
     * @param foodId 음식 ID
     * @return 음식 상세 정보
     */
    @GetMapping("/{foodId}")
    public ApiResponse<FoodResponse> getFood(
            @PathVariable @Positive Long foodId
    ) {
        log.info("[ADMIN] GET /api/v1/admin/foods/{} - foodId: {}", foodId, foodId);
        
        FoodServiceResponse serviceResponse = foodApplicationService.getFood(foodId);
        FoodResponse response = FoodResponse.from(serviceResponse);
        
        return ApiResponse.success(response);
    }

    /**
     * 음식 생성
     * 
     * @param storeId 가게 ID (필수)
     * @param request 음식 생성 요청
     * @return 생성된 음식 정보
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FoodResponse> createFood(
            @PathVariable @Positive Long storeId,
            @RequestBody @Valid CreateFoodRequest request
    ) {
        log.info("[ADMIN] POST /api/v1/admin/stores/{}/foods - name: {}", 
                storeId, request.foodName());
        
        // request body에서 storeId를 pathVariable에서 전달받은 값으로 사용
        FoodServiceResponse serviceResponse = foodApplicationService.createFood(
                request.withStoreId(storeId).toServiceRequest()
        );
        FoodResponse response = FoodResponse.from(serviceResponse);
        
        return ApiResponse.success(response);
    }

    /**
     * 음식 수정
     * 
     * @param foodId 음식 ID
     * @param request 음식 수정 요청
     * @return 수정된 음식 정보
     */
    @PutMapping("/{foodId}")
    public ApiResponse<FoodResponse> updateFood(
            @PathVariable @Positive Long foodId,
            @RequestBody @Valid UpdateFoodRequest request
    ) {
        log.info("[ADMIN] PUT /api/v1/admin/foods/{} - name: {}", 
                foodId, request.foodName());
        
        FoodServiceResponse serviceResponse = foodApplicationService.updateFood(
                foodId,
                request.toServiceRequest()
        );
        FoodResponse response = FoodResponse.from(serviceResponse);
        
        return ApiResponse.success(response);
    }

    /**
     * 음식 삭제 (논리적 삭제)
     * 
     * @param storeId 가게 ID
     * @param foodId 음식 ID
     */
    @DeleteMapping("/{foodId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFood(
            @PathVariable @Positive Long storeId,
            @PathVariable @Positive Long foodId
    ) {
        log.info("[ADMIN] DELETE /api/v1/admin/stores/{}/foods/{} - foodId: {}", storeId, foodId, foodId);
        
        foodApplicationService.deleteFood(foodId);
    }
}
