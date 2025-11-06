package com.stdev.smartmealtable.admin.store.controller.request;

import com.stdev.smartmealtable.domain.store.StoreType;
import jakarta.validation.constraints.*;

/**
 * 음식점 생성 요청 DTO (Controller) - v2.0
 * 
 * <p>위도/경도는 서버에서 지오코딩 API를 통해 자동 처리됩니다.</p>
 * <p>이미지는 별도 StoreImage API로 관리됩니다.</p>
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

        @Size(max = 20, message = "전화번호는 최대 20자까지 입력 가능합니다.")
        String phoneNumber,

        String description,

        @Min(value = 0, message = "평균 가격은 0 이상이어야 합니다.")
        Integer averagePrice,

        @NotNull(message = "음식점 유형은 필수입니다.")
        StoreType storeType
) {
}
