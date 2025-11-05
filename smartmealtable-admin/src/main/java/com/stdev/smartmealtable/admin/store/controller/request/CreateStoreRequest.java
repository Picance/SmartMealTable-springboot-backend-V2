package com.stdev.smartmealtable.admin.store.controller.request;

import com.stdev.smartmealtable.domain.store.StoreType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 음식점 생성 요청 DTO (Controller)
 */
public record CreateStoreRequest(
        @NotBlank(message = "음식점 이름은 필수입니다.")
        @Size(max = 100, message = "음식점 이름은 최대 100자까지 입력 가능합니다.")
        String name,

        @NotNull(message = "카테고리 ID는 필수입니다.")
        Long categoryId,

        Long sellerId, // 선택 필드

        @NotBlank(message = "도로명 주소는 필수입니다.")
        @Size(max = 200, message = "도로명 주소는 최대 200자까지 입력 가능합니다.")
        String address,

        @Size(max = 200, message = "지번 주소는 최대 200자까지 입력 가능합니다.")
        String lotNumberAddress,

        @NotNull(message = "위도는 필수입니다.")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
        BigDecimal latitude,

        @NotNull(message = "경도는 필수입니다.")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
        BigDecimal longitude,

        @Size(max = 20, message = "전화번호는 최대 20자까지 입력 가능합니다.")
        String phoneNumber,

        String description,

        @Min(value = 0, message = "평균 가격은 0 이상이어야 합니다.")
        Integer averagePrice,

        @NotNull(message = "음식점 유형은 필수입니다.")
        StoreType storeType,

        @Size(max = 500, message = "이미지 URL은 최대 500자까지 입력 가능합니다.")
        String imageUrl
) {
}
