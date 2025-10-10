package com.stdev.smartmealtable.api.member.service.social;

import com.stdev.smartmealtable.domain.member.entity.SocialAccount;
import com.stdev.smartmealtable.domain.member.entity.SocialProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 소셜 계정 추가 연동 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddSocialAccountServiceResponse {

    private Long socialAccountId;
    private SocialProvider provider;
    private String providerEmail;
    private LocalDateTime connectedAt;

    public static AddSocialAccountServiceResponse of(SocialAccount socialAccount, String providerEmail) {
        AddSocialAccountServiceResponse response = new AddSocialAccountServiceResponse();
        response.socialAccountId = socialAccount.getSocialAccountId();
        response.provider = socialAccount.getProvider();
        response.providerEmail = providerEmail;
        response.connectedAt = socialAccount.getConnectedAt();
        return response;
    }
}
