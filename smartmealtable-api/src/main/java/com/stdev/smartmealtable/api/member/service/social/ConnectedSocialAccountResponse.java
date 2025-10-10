package com.stdev.smartmealtable.api.member.service.social;

import com.stdev.smartmealtable.domain.member.entity.SocialAccount;
import com.stdev.smartmealtable.domain.member.entity.SocialProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 연동된 소셜 계정 응답 DTO (단일 계정)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectedSocialAccountResponse {

    private Long socialAccountId;
    private SocialProvider provider;
    private String providerEmail;
    private LocalDateTime connectedAt;

    public static ConnectedSocialAccountResponse from(SocialAccount socialAccount, String providerEmail) {
        ConnectedSocialAccountResponse response = new ConnectedSocialAccountResponse();
        response.socialAccountId = socialAccount.getSocialAccountId();
        response.provider = socialAccount.getProvider();
        response.providerEmail = providerEmail;
        response.connectedAt = socialAccount.getConnectedAt();
        return response;
    }
}
