package com.stdev.smartmealtable.admin.category.controller.response;

import com.stdev.smartmealtable.admin.category.service.dto.CategoryListServiceResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 카테고리 목록 응답 DTO (페이징 포함)
 */
public record CategoryListResponse(
        List<CategoryResponse> categories,
        PageInfo pageInfo
) {
    public static CategoryListResponse from(CategoryListServiceResponse serviceResponse) {
        List<CategoryResponse> categories = serviceResponse.categories().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
        
        PageInfo pageInfo = new PageInfo(
                serviceResponse.page(),
                serviceResponse.size(),
                serviceResponse.totalElements(),
                serviceResponse.totalPages()
        );
        
        return new CategoryListResponse(categories, pageInfo);
    }
    
    /**
     * 페이징 정보
     */
    public record PageInfo(
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}
