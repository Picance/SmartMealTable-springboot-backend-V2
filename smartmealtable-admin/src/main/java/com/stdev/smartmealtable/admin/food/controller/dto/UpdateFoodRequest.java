package com.stdev.smartmealtable.admin.food.controller.dto;

import com.stdev.smartmealtable.admin.food.service.dto.UpdateFoodServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 음식 수정 요청 DTO
 */
public record UpdateFoodRequest(
        @NotBlank(message = "음식 이름은 필수입니다.")
        @Size(max = 100, message = "음식 이름은 100자를 초과할 수 없습니다.")
        String foodName,

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

        Boolean isMain,

        Integer displayOrder
) {
    public UpdateFoodServiceRequest toServiceRequest() {
        return new UpdateFoodServiceRequest(
                foodName,
                categoryId,
                description,
                imageUrl,
                averagePrice,
                isMain,
                displayOrder
        );
    }
}
