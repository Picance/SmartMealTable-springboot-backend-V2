package com.stdev.smartmealtable.admin.category.controller;

import com.stdev.smartmealtable.admin.category.controller.request.CreateCategoryRequest;
import com.stdev.smartmealtable.admin.category.controller.request.UpdateCategoryRequest;
import com.stdev.smartmealtable.admin.category.controller.response.CategoryListResponse;
import com.stdev.smartmealtable.admin.category.controller.response.CategoryResponse;
import com.stdev.smartmealtable.admin.category.service.CategoryApplicationService;
import com.stdev.smartmealtable.admin.category.service.dto.CategoryListServiceRequest;
import com.stdev.smartmealtable.admin.category.service.dto.CategoryListServiceResponse;
import com.stdev.smartmealtable.admin.category.service.dto.CategoryServiceResponse;
import com.stdev.smartmealtable.admin.category.service.dto.CreateCategoryServiceRequest;
import com.stdev.smartmealtable.admin.category.service.dto.UpdateCategoryServiceRequest;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 카테고리 관리 API Controller (ADMIN)
 * 
 * <p>관리자가 음식 카테고리를 관리하는 API를 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CategoryController {

    private final CategoryApplicationService categoryApplicationService;

    /**
     * 카테고리 목록 조회 (페이징)
     *
     * @param name 검색할 카테고리 이름 (선택)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 카테고리 목록
     */
    @GetMapping
    public ApiResponse<CategoryListResponse> getCategories(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    ) {
        CategoryListServiceRequest serviceRequest = CategoryListServiceRequest.of(name, page, size);
        CategoryListServiceResponse serviceResponse = categoryApplicationService.getCategories(serviceRequest);
        
        CategoryListResponse response = CategoryListResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 카테고리 상세 조회
     *
     * @param categoryId 카테고리 ID
     * @return 카테고리 상세 정보
     */
    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getCategory(
            @PathVariable Long categoryId
    ) {
        CategoryServiceResponse serviceResponse = categoryApplicationService.getCategory(categoryId);
        
        CategoryResponse response = CategoryResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 카테고리 생성
     *
     * @param request 카테고리 생성 요청
     * @return 생성된 카테고리 정보
     */
    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CreateCategoryServiceRequest serviceRequest = CreateCategoryServiceRequest.of(request.name());
        CategoryServiceResponse serviceResponse = categoryApplicationService.createCategory(serviceRequest);
        
        CategoryResponse response = CategoryResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 카테고리 수정
     *
     * @param categoryId 카테고리 ID
     * @param request    카테고리 수정 요청
     * @return 수정된 카테고리 정보
     */
    @PutMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        UpdateCategoryServiceRequest serviceRequest = UpdateCategoryServiceRequest.of(request.name());
        CategoryServiceResponse serviceResponse = categoryApplicationService.updateCategory(categoryId, serviceRequest);
        
        CategoryResponse response = CategoryResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 카테고리 삭제 (물리적 삭제)
     *
     * @param categoryId 카테고리 ID
     * @return 성공 응답
     */
    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(
            @PathVariable Long categoryId
    ) {
        categoryApplicationService.deleteCategory(categoryId);
        return ApiResponse.success();
    }
}
