package com.stdev.smartmealtable.admin.policy.controller.dto;

import com.stdev.smartmealtable.admin.policy.service.dto.CreatePolicyServiceRequest;
import com.stdev.smartmealtable.domain.policy.entity.PolicyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 약관 생성 Request DTO
 */
public record CreatePolicyRequest(
        @NotBlank(message = "약관 제목은 필수입니다")
        @Size(max = 100, message = "약관 제목은 100자를 초과할 수 없습니다")
        String title,

        @NotBlank(message = "약관 내용은 필수입니다")
        String content,

        @NotNull(message = "약관 타입은 필수입니다")
        PolicyType type,

        @NotBlank(message = "약관 버전은 필수입니다")
        @Size(max = 20, message = "약관 버전은 20자를 초과할 수 없습니다")
        String version,

        @NotNull(message = "필수 동의 여부는 필수입니다")
        Boolean isMandatory
) {
    public CreatePolicyServiceRequest toServiceRequest() {
        return new CreatePolicyServiceRequest(
                title,
                content,
                type,
                version,
                isMandatory
        );
    }
}
