package com.stdev.smartmealtable.api.onboarding.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 약관 동의 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PolicyAgreementRequest {

    @NotEmpty(message = "최소 1개 이상의 약관에 동의해야 합니다.")
    @Valid
    private List<AgreementItem> agreements;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class AgreementItem {
        
        @NotNull(message = "약관 ID는 필수입니다.")
        private Long policyId;

        @NotNull(message = "동의 여부는 필수입니다.")
        private Boolean isAgreed;
    }
}
