package com.stdev.smartmealtable.api.member.service.social;

import com.stdev.smartmealtable.domain.member.entity.SocialProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 소셜 계정 추가 연동 요청 DTO
 */
public record AddSocialAccountServiceRequest(
        @NotNull(message = "소셜 제공자를 입력해주세요.")
        SocialProvider provider,

        @NotBlank(message = "인증 코드를 입력해주세요.")
        String authorizationCode
) {
}
