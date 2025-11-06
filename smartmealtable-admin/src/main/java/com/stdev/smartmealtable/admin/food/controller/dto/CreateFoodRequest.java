package com.stdev.smartmealtable.admin.food.controller.dto;

import com.stdev.smartmealtable.admin.food.service.dto.CreateFoodServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 음식 생성 요청 DTO - v2.0
 * 
 * <p>isMain, displayOrder 필드가 추가되었습니다.</p>
 */
public record CreateFoodRequest(
        @NotBlank(message = "음식 이름은 필수입니다.")
        @Size(max = 100, message = "음식 이름은 100자를 초과할 수 없습니다.")
        String foodName,

        @NotNull(message = "가게 ID는 필수입니다.")
        @Positive(message = "가게 ID는 양수여야 합니다.")
        Long storeId,

        @NotNull(message = "카테고리 ID는 필수입니다.")
        @Positive(message = "카테고리 ID는 양수여야 합니다.")
        Long categoryId,

        @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
        String description,

        @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다.")
        String imageUrl,

        @NotNull(message = "평균 가격은 필수입니다.")
        @Positive(message = "평균 가격은 양수여야 합니다.")
        Integer averagePrice,
        
        Boolean isMain, // 대표 메뉴 여부 (null이면 false로 처리)
        
        Integer displayOrder // 표시 순서 (null이면 자동 할당)
) {
    public CreateFoodServiceRequest toServiceRequest() {
        return new CreateFoodServiceRequest(
                foodName,
                storeId,
                categoryId,
                description,
                imageUrl,
                averagePrice,
                isMain != null ? isMain : false,
                displayOrder
        );
    }
}
