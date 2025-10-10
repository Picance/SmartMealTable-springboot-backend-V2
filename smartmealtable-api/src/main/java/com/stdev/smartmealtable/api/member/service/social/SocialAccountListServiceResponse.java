package com.stdev.smartmealtable.api.member.service.social;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 소셜 계정 목록 조회 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SocialAccountListServiceResponse {

    private List<ConnectedSocialAccountResponse> connectedAccounts;
    private Boolean hasPassword;

    public static SocialAccountListServiceResponse of(
            List<ConnectedSocialAccountResponse> connectedAccounts,
            Boolean hasPassword
    ) {
        SocialAccountListServiceResponse response = new SocialAccountListServiceResponse();
        response.connectedAccounts = connectedAccounts;
        response.hasPassword = hasPassword;
        return response;
    }
}
