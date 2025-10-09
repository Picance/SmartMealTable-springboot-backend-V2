package com.stdev.smartmealtable.api.onboarding.controller.dto;

import com.stdev.smartmealtable.api.onboarding.service.dto.GetFoodsServiceResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 음식 목록 조회 API Response
 */
public record GetFoodsResponse(
        List<FoodInfo> content,
        PageableInfo pageable,
        long totalElements,
        int totalPages,
        boolean last,
        int size,
        int number,
        boolean first,
        int numberOfElements,
        boolean empty
) {
    /**
     * Service Response → Controller Response 변환
     */
    public static GetFoodsResponse from(GetFoodsServiceResponse serviceResponse) {
        List<FoodInfo> foodInfos = serviceResponse.content().stream()
                .map(f -> new FoodInfo(
                        f.foodId(),
                        f.foodName(),
                        f.categoryId(),
                        f.categoryName(),
                        f.imageUrl(),
                        f.description(),
                        f.averagePrice()
                ))
                .collect(Collectors.toList());

        PageableInfo pageableInfo = new PageableInfo(
                serviceResponse.pageNumber(),
                serviceResponse.pageSize(),
                new SortInfo(false, true, true),
                serviceResponse.pageNumber() * serviceResponse.pageSize(),
                true,
                false
        );

        return new GetFoodsResponse(
                foodInfos,
                pageableInfo,
                serviceResponse.totalElements(),
                serviceResponse.totalPages(),
                serviceResponse.last(),
                serviceResponse.pageSize(),
                serviceResponse.pageNumber(),
                serviceResponse.first(),
                foodInfos.size(),
                foodInfos.isEmpty()
        );
    }

    /**
     * 음식 정보
     */
    public record FoodInfo(
            Long foodId,
            String foodName,
            Long categoryId,
            String categoryName,
            String imageUrl,
            String description,
            Integer averagePrice
    ) {
    }

    /**
     * 페이징 정보
     */
    public record PageableInfo(
            int pageNumber,
            int pageSize,
            SortInfo sort,
            long offset,
            boolean paged,
            boolean unpaged
    ) {
    }

    /**
     * 정렬 정보
     */
    public record SortInfo(
            boolean sorted,
            boolean unsorted,
            boolean empty
    ) {
    }
}
