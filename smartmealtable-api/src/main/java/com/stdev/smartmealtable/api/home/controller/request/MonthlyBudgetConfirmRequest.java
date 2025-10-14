package com.stdev.smartmealtable.api.home.controller.request;

import jakarta.validation.constraints.*;

/**
 * 월별 예산 확인 처리 요청 DTO
 *
 * @param year   연도
 * @param month  월 (1-12)
 * @param action 사용자 액션 (KEEP/CHANGE)
 */
public record MonthlyBudgetConfirmRequest(
        @NotNull(message = "연도는 필수입니다.")
        @Min(value = 2000, message = "연도는 2000 이상이어야 합니다.")
        @Max(value = 9999, message = "연도는 9999 이하여야 합니다.")
        Integer year,

        @NotNull(message = "월은 필수입니다.")
        @Min(value = 1, message = "월은 1 이상이어야 합니다.")
        @Max(value = 12, message = "월은 12 이하여야 합니다.")
        Integer month,

        @NotBlank(message = "액션은 필수입니다.")
        @Pattern(regexp = "KEEP|CHANGE", message = "액션은 KEEP 또는 CHANGE만 가능합니다.")
        String action
) {
}
