package com.stdev.smartmealtable.api.category.controller;

import com.stdev.smartmealtable.api.category.controller.dto.CategoryResponse;
import com.stdev.smartmealtable.api.category.service.GetCategoriesService;
import com.stdev.smartmealtable.api.category.service.dto.GetCategoriesServiceResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 카테고리 관리 Controller
 * 온보딩 시 음식 카테고리 선택을 위한 카테고리 목록 제공
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final GetCategoriesService getCategoriesService;

    /**
     * 카테고리 목록 조회
     * GET /api/v1/categories
     *
     * @return 모든 카테고리 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CategoryListResponse>> getCategories() {
        log.info("카테고리 목록 조회 API 호출");

        GetCategoriesServiceResponse serviceResponse = getCategoriesService.getCategories();

        // Response DTO 변환
        List<CategoryResponse> categories = serviceResponse.categories().stream()
                .map(c -> new CategoryResponse(c.categoryId(), c.name()))
                .collect(Collectors.toList());

        CategoryListResponse response = new CategoryListResponse(categories);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 카테고리 목록 응답 DTO
     */
    public record CategoryListResponse(
            List<CategoryResponse> categories
    ) {
    }
}
