package com.stdev.smartmealtable.admin.category.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 카테고리 생성 요청 DTO
 */
public record CreateCategoryRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Size(max = 50, message = "카테고리 이름은 최대 50자까지 입력 가능합니다.")
        String name
) {
}
