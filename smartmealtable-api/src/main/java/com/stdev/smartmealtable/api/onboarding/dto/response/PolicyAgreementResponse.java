package com.stdev.smartmealtable.api.onboarding.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 약관 동의 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PolicyAgreementResponse {

    private Long memberAuthenticationId;
    private Integer agreedCount;
    private String message;

    public static PolicyAgreementResponse of(
            Long memberAuthenticationId,
            Integer agreedCount
    ) {
        return new PolicyAgreementResponse(
                memberAuthenticationId,
                agreedCount,
                "약관 동의가 성공적으로 완료되었습니다."
        );
    }
}
