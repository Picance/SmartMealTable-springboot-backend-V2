package com.stdev.smartmealtable.api.onboarding.service.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 약관 동의 Service 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PolicyAgreementServiceRequest {

    private Long policyId;
    private Boolean isAgreed;
}
