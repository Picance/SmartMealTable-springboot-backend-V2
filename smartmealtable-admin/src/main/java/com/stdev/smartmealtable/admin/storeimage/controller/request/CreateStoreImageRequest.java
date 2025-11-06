package com.stdev.smartmealtable.admin.storeimage.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 가게 이미지 추가 요청 DTO
 */
public record CreateStoreImageRequest(
        @NotBlank(message = "이미지 URL은 필수입니다.")
        @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다.")
        String imageUrl,
        
        @NotNull(message = "대표 이미지 여부는 필수입니다.")
        Boolean isMain,
        
        Integer displayOrder // null이면 자동 할당
) {
}
