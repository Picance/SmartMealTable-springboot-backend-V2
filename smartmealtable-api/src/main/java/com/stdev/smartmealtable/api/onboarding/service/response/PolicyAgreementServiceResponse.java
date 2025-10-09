package com.stdev.smartmealtable.api.onboarding.service.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 약관 동의 Service 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PolicyAgreementServiceResponse {

    private Long memberAuthenticationId;
    private Integer agreedCount;
}
